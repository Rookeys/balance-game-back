/*
 * File Name   : GamePlayCountCacheRepository.java
 * Description : 게임별 플레이 카운트 캐시 조회 Repository (Spring AOP self-invocation 방지용 분리 빈)
 *
 * Created By  : cheomuk
 * Created At  : 2026-05-04
 * Updated At  : 2026-05-10
 *
 * Change Log
 * -------------------------------------------------
 * 2026-05-04  최초 생성
 * 2026-05-10  @Cacheable 제거, Redis Hash 직접 읽기 + DB fallback으로 교체
 */
package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.domain.game.enums.GameSortType;
import com.games.balancegameback.infra.repository.game.common.GamePlayCounts;
import com.games.balancegameback.infra.repository.game.scheduler.GamePlayCountScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GamePlayCountCacheRepository {

    private final RedisTemplate<String, Object> hashRedisTemplate;
    private final GamePlayCountBatchRepository batchRepository;

    @SuppressWarnings("unchecked")
    public Map<Long, GamePlayCounts> getBulkPlayCounts(List<Long> gameIds, GameSortType sortType) {
        try {
            long rStart = System.currentTimeMillis();
            List<Object> pipelineResults = hashRedisTemplate.executePipelined(new SessionCallback<Object>() {
                @Override
                public Object execute(RedisOperations operations) {
                    for (Long gameId : gameIds) {
                        operations.opsForHash().entries(GamePlayCountScheduler.KEY_PREFIX + gameId);
                    }
                    return null;
                }
            });
            log.info("[PERF]   Redis pipeline round-trip: {}ms", System.currentTimeMillis() - rStart);

            Map<Long, GamePlayCounts> result = new HashMap<>();
            List<Long> missingIds = new ArrayList<>();

            for (int i = 0; i < gameIds.size(); i++) {
                Long gameId = gameIds.get(i);
                Object raw = pipelineResults.get(i);
                if (raw instanceof Map<?, ?> hashMap && !hashMap.isEmpty()) {
                    result.put(gameId, parseGamePlayCounts((Map<String, Object>) hashMap, sortType));
                } else {
                    missingIds.add(gameId);
                }
            }

            if (!missingIds.isEmpty()) {
                log.info("[PERF] getPlayCounts Redis miss for {} ids, falling back to DB", missingIds.size());
                result.putAll(fetchFromDbBulk(missingIds, sortType));
            } else {
                log.info("[PERF] getPlayCounts Redis hit for all {} ids", gameIds.size());
            }
            return result;
        } catch (Exception e) {
            log.warn("[GamePlayCountCacheRepository] Redis bulk 조회 실패, DB fallback", e);
            return fetchFromDbBulk(gameIds, sortType);
        }
    }

    private GamePlayCounts parseGamePlayCounts(Map<String, Object> hash, GameSortType sortType) {
        int total = toInt(hash.get("total"));
        int week  = (sortType == GameSortType.WEEK || sortType == GameSortType.MONTH)
                ? toInt(hash.get("week")) : 0;
        int month = (sortType == GameSortType.MONTH)
                ? toInt(hash.get("month")) : 0;
        return new GamePlayCounts(total, week, month);
    }

    private Map<Long, GamePlayCounts> fetchFromDbBulk(List<Long> gameIds, GameSortType sortType) {
        Map<Long, Integer> totalMap = batchRepository.fetchTotalCountsByIds(gameIds);
        Map<Long, Integer> weekMap  = (sortType == GameSortType.WEEK || sortType == GameSortType.MONTH)
                ? batchRepository.fetchWeekCountsByIds(gameIds) : Map.of();
        Map<Long, Integer> monthMap = (sortType == GameSortType.MONTH)
                ? batchRepository.fetchMonthCountsByIds(gameIds) : Map.of();

        Map<Long, GamePlayCounts> result = new HashMap<>();
        for (Long gameId : gameIds) {
            result.put(gameId, new GamePlayCounts(
                    totalMap.getOrDefault(gameId, 0),
                    weekMap.getOrDefault(gameId, 0),
                    monthMap.getOrDefault(gameId, 0)
            ));
        }
        return result;
    }

    private int toInt(Object value) {
        if (value instanceof Integer i) return i;
        if (value instanceof Long l) return l.intValue();
        if (value instanceof Number n) return n.intValue();
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ex) {
            log.warn("[GamePlayCountCacheRepository] 예상치 못한 값 타입: {}", value);
            return 0;
        }
    }
}
