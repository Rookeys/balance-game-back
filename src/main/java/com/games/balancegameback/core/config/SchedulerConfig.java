package com.games.balancegameback.core.config;

import com.games.balancegameback.service.game.repository.GameCommentLikesRepository;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.Set;
import java.util.concurrent.*;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulerConfig {

    private final StringRedisTemplate redisTemplate;
    private final GameCommentLikesRepository gameCommentLikesRepository;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(10);

    private static final String RESOURCE_LIKE_KEY = "like:resource_comment:";
    private static final String RESULT_LIKE_KEY = "like:result_comment:";

    @PostConstruct
    public void initSchedulers() {
        // 1️⃣ 게임 리소스 댓글 좋아요 배치 업데이트
        scheduler.scheduleAtFixedRate(this::batchUpdateResourceCommentLikes, 0, 60, TimeUnit.SECONDS);

        // 2️⃣ 게임 결과 댓글 좋아요 배치 업데이트
        scheduler.scheduleAtFixedRate(this::batchUpdateResultCommentLikes, 0, 60, TimeUnit.SECONDS);
    }

    /**
     * 게임 리소스 댓글 좋아요 배치 업데이트
     */
    private void batchUpdateResourceCommentLikes() {
        Set<String> keys = redisTemplate.keys(RESOURCE_LIKE_KEY);

        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            Long commentId = Long.parseLong(key.split(":")[2]);
            int likeCount = Integer.parseInt(redisTemplate.opsForValue().get(key));

            redisTemplate.delete(key);
        }
    }

    /**
     * 게임 결과 댓글 좋아요 배치 업데이트
     */
    private void batchUpdateResultCommentLikes() {
        Set<String> keys = redisTemplate.keys(RESULT_LIKE_KEY);

        if (keys == null || keys.isEmpty()) {
            return;
        }

        for (String key : keys) {
            Long commentId = Long.parseLong(key.split(":")[2]);
            int likeCount = Integer.parseInt(redisTemplate.opsForValue().get(key));

            redisTemplate.delete(key);
        }
    }

    @PreDestroy
    public void shutdownScheduler() {
        scheduler.shutdown();
    }
}

