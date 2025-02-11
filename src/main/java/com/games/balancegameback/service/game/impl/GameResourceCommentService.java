package com.games.balancegameback.service.game.impl;

import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.GameResourceComments;
import com.games.balancegameback.service.game.repository.GameResourceCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class GameResourceCommentService {

//    private final GameResourceCommentRepository commentsRepository;
    private final StringRedisTemplate redisTemplate;

    private static final String LIKE_KEY = "like:gameResourceComment:";
    private static final String USER_LIKE_KEY = "like:user:%d:gameResourceComment:%d";

//    @Transactional
//    public void addComment() {
//
//    }
//
//    @Transactional
//    public boolean toggleLike(Long userId, Long commentId) {
//        String userLikeKey = String.format(USER_LIKE_KEY, userId, commentId);
//        String likeKey = LIKE_KEY + commentId;
//
//        // 좋아요 상태 확인
//        boolean hasLiked = redisTemplate.hasKey(userLikeKey) != null;
//
//        if (hasLiked) {
//            // 이미 좋아요 눌렀다면 취소
//            commentsRepository.decreaseLikeCount(commentId);
//            redisTemplate.delete(userLikeKey);
//            redisTemplate.opsForValue().decrement(likeKey);
//            return false;
//        } else {
//            // 좋아요 추가 (디바운싱: 5초 동안 중복 요청 방지)
//            commentsRepository.increaseLikeCount(commentId);
//            redisTemplate.opsForValue().set(userLikeKey, "1", 5, TimeUnit.SECONDS);
//            redisTemplate.opsForValue().increment(likeKey);
//            return true;
//        }
//    }
//
//    public CustomPageImpl<GameResourceComments> getCommentsByGameResource(Long gameResourceId) {
//        return commentsRepository.findByGameResourcesId(gameResourceId);
//    }
}
