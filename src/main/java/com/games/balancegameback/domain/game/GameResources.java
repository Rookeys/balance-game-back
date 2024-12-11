package com.games.balancegameback.domain.game;

import com.games.balancegameback.domain.media.Media;
import lombok.Builder;

public record GameResources(Long id, String title, Games games, Media media) {

    @Builder
    public GameResources {

    }
}
