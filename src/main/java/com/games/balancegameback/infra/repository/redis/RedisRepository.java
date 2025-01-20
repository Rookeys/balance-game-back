package com.games.balancegameback.infra.repository.redis;

public interface RedisRepository {

    void setValues(String token, String email);

    String getValues(String token);

    boolean isRefreshTokenValid(String token);

    void delValues(String token);
}
