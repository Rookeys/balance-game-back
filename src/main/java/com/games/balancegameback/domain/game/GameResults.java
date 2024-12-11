package com.games.balancegameback.domain.game;

import com.games.balancegameback.domain.media.Media;
import lombok.Builder;

public record GameResults(Long id, Games games, Media media) {

    @Builder
    public GameResults {

    }
}
