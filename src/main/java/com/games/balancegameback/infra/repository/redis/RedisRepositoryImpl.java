package com.games.balancegameback.infra.repository.redis;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.InvalidTokenException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RedisRepositoryImpl implements RedisRepository {

    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * RefreshToken 및 이메일 저장
     * @param token Refresh Token
     * @param email 사용자 이메일
     */
    public void setValues(String token, String email) {
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        Map<String, String> tokenData = new HashMap<>();
        tokenData.put("email", email);
        operations.set(token, tokenData, Duration.ofDays(7)); // 7일 TTL
    }

    /**
     * RefreshToken으로 이메일 및 관련 데이터 가져오기
     * @param token Refresh Token
     * @return 이메일 및 관련 데이터 맵
     */
    public Map<String, String> getValues(String token) {
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        Object object = operations.get(token);
        if (object instanceof Map) {
            return (Map<String, String>) object;
        }
        return null;
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
     * 블랙리스트에 포함된 토큰인지 확인
     * @param token 액세스 토큰
     * @return 유효 여부
     */
    public boolean isTokenInBlacklist(String token) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(token))) {
            throw new InvalidTokenException("4001", ErrorCode.INVALID_TOKEN_EXCEPTION);
        }

        return false;
    }

    /**
     * 토큰을 블랙리스트에 추가
     * @param token 액세스 토큰
     * @param expiration 만료 시간 (밀리초)
     */
    public void addTokenToBlacklist(String token, long expiration) {
        ValueOperations<String, Object> operations = redisTemplate.opsForValue();
        operations.set(token, true, expiration, TimeUnit.MILLISECONDS);
    }

    /**
     * RefreshToken 삭제
     * @param token Refresh Token
     */
    public void delValues(String token) {
        redisTemplate.delete(token);
    }
}

