package com.games.balancegameback.infra.repository.game.common;

public final class GameConstants {

    // 게임 검증 관련
    public static final int MIN_RESOURCE_COUNT = 2;
    public static final int TOP_RESOURCE_LIMIT = 2;

    // 사용자 관련
    public static final String ANONYMOUS_NICKNAME = "익명";

    // 기본값 관련
    public static final long DEFAULT_COUNT = 0L;
    public static final int DEFAULT_SEC = 0;

    // 최근 플레이 관련
    public static final int MAX_RECENT_PLAYS = 50;

    private GameConstants() {
        throw new AssertionError("Constants class should not be instantiated");
    }
}
