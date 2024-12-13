package com.games.balancegameback.infra.repository.redis;

import java.util.Map;

public interface RedisRepository {

    void setValues(String token, String email);

    Map<String, String> getValues(String token);

    boolean isRefreshTokenValid(String token);

    boolean isTokenInBlacklist(String token);

    void addTokenToBlacklist(String token, long expiration);

    void delValues(String token);
}
