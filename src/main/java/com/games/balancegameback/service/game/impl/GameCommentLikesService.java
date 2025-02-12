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

    private static final String LIKE_KEY = "like:gameResourceComment:";
    private static final String USER_LIKE_KEY = "like:user:%d:gameResourceComment:%d";

    @Transactional
    public void toggleLike(Long commentId) {

    }
}
