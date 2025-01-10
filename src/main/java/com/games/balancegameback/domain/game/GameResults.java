package com.games.balancegameback.domain.game;

import lombok.Builder;

public record GameResults(Long id, GameResources gameResources) {

    @Builder
    public GameResults {

    }
}
