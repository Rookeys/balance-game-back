package com.games.balancegameback.infra.repository.redis;

public interface RedisRepository {

    void setValues(String token, String email);

    String getValues(String token);

    boolean isRefreshTokenValid(String token);

    boolean isTokenInBlacklist(String token);

    void addTokenToBlacklist(String token, long expiration);

    void delValues(String token);
}
