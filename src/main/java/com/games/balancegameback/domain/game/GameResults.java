package com.games.balancegameback.domain.game;

import lombok.Builder;

public record GameResults(String id, GameResources gameResources) {

    @Builder
    public GameResults {

    }
}