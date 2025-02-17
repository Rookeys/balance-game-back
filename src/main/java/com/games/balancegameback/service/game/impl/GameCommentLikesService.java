package com.games.balancegameback.service.game.impl;

import com.games.balancegameback.service.game.repository.GameCommentLikesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class GameCommentLikesService {

    private final StringRedisTemplate redisTemplate;
    private final GameCommentLikesRepository gameCommentLikesRepository;

    private static final String LIKE_COUNT_KEY = "like:comment:";
    private static final String USER_LIKE_KEY = "like:email:%d:comment:%d";
    private static final long EXPIRE_TIME = 10; // Redis 데이터 만료 시간(초)

    @Transactional
    public boolean toggleLike(Long userId, Long commentId) {
        String userLikeKey = String.format(USER_LIKE_KEY, userId, commentId);
        String likeCountKey = LIKE_COUNT_KEY + commentId;

        // Redis 에 저장된 기록이 있는지 확인
        Boolean hasLiked = redisTemplate.hasKey(userLikeKey);

        if (Boolean.TRUE.equals(hasLiked)) {
            // 이미 좋아요를 눌렀던 기록이 있으면 취소
            redisTemplate.delete(userLikeKey);
            redisTemplate.opsForValue().decrement(likeCountKey);
            return false;
        } else {
            // 좋아요 추가
            redisTemplate.opsForValue().set(userLikeKey, "1", EXPIRE_TIME, TimeUnit.SECONDS);
            redisTemplate.opsForValue().increment(likeCountKey);
            return true;
        }
    }
}
