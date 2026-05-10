/*
 * File Name   : GamePlayCountScheduler.java
 * Description : 5분 주기로 게임 플레이 카운트를 전수 갱신해 Redis Hash에 저장하는 스케줄러
 *
 * Created By  : cheomuk
 * Created At  : 2026-05-10
 * Updated At  : 2026-05-10
 *
 * Change Log
 * -------------------------------------------------
 * 2026-05-10  최초 생성
 */
package com.games.balancegameback.infra.repository.game.scheduler;

import com.games.balancegameback.infra.repository.game.impl.GamePlayCountBatchRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class GamePlayCountScheduler {

    public static final String KEY_PREFIX = "game:play-counts:";

    private final GamePlayCountBatchRepository batchRepository;
    private final RedisTemplate<String, Object> hashRedisTemplate;

    @Scheduled(initialDelay = 0, fixedRate = 300_000)
    public void refreshPlayCounts() {
        log.info("[GamePlayCountScheduler] 플레이 카운트 전수 갱신 시작");
        long start = System.currentTimeMillis();
        try {
            Map<Long, Integer> totalMap = batchRepository.fetchTotalCounts();
            Map<Long, Integer> weekMap  = batchRepository.fetchWeekCounts();
            Map<Long, Integer> monthMap = batchRepository.fetchMonthCounts();
            HashOperations<String, String, Object> hashOps = hashRedisTemplate.opsForHash();
            for (Long gameId : totalMap.keySet()) {
                String key = KEY_PREFIX + gameId;
                Map<String, Object> fields = new HashMap<>();
                fields.put("total", totalMap.getOrDefault(gameId, 0));
                fields.put("week",  weekMap.getOrDefault(gameId, 0));
                fields.put("month", monthMap.getOrDefault(gameId, 0));
                hashOps.putAll(key, fields);
            }
            log.info("[GamePlayCountScheduler] 완료. 갱신 게임 수: {}, 소요: {}ms",
                    totalMap.size(), System.currentTimeMillis() - start);
        } catch (Exception e) {
            log.error("[GamePlayCountScheduler] 전수 갱신 중 오류 발생", e);
        }
    }
}
