package com.games.balancegameback.infra.repository.game.common;

/**
 * 게임 플레이 카운트 데이터
 */
public record GamePlayCounts(
        int totalPlays,
        int weekPlays,
        int monthPlays
) {}
