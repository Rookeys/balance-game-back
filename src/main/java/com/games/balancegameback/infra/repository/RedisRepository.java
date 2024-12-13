package com.games.balancegameback.infra.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;

}
