package com.games.balancegameback.domain.game;

import com.games.balancegameback.domain.media.Images;
import com.games.balancegameback.domain.media.Links;
import lombok.Builder;

public record GameResources(Long id, String title, Games games, Links links, Images images) {

    @Builder
    public GameResources {

    }
}
