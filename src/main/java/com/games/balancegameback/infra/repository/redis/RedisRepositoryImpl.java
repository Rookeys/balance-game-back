package com.games.balancegameback.infra.repository.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;

@Repository
@RequiredArgsConstructor
public class RedisRepositoryImpl implements RedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * RefreshToken 및 이메일 저장
     * @param token Refresh Token
     * @param email 사용자 이메일
     */
    public void setValues(String token, String email) {
        ValueOperations<String, String> operations = redisTemplate.opsForValue();
        operations.set(token, email, Duration.ofDays(7)); // 7일 TTL
    }

    /**
     * RefreshToken으로 이메일 및 관련 데이터 가져오기
     * @param token Refresh Token
     * @return 이메일 및 관련 데이터 맵
     */
    public String getValues(String token) {
        return redisTemplate.opsForValue().get(token);
    }

    /**
     * RefreshToken 유효성 확인
     * @param token Refresh Token
     * @return 유효 여부
     */
    public boolean isRefreshTokenValid(String token) {
        return getValues(token) != null;
    }

    /**
     * RefreshToken 삭제
     * @param token Refresh Token
     */
    public void delValues(String token) {
        redisTemplate.delete(token);
    }
}

