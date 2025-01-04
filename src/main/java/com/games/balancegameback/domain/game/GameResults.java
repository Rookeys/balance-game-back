package com.games.balancegameback.domain.game;

import com.games.balancegameback.domain.media.Images;
import com.games.balancegameback.domain.media.Links;
import lombok.Builder;

public record GameResults(Long id, Games games, Images images, Links links) {

    @Builder
    public GameResults {

    }
}
