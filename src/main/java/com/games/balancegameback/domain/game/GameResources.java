package com.games.balancegameback.domain.game;

import com.games.balancegameback.domain.media.Images;
import com.games.balancegameback.domain.media.Links;
import lombok.Builder;
import lombok.Getter;

@Getter
public class GameResources {

    private final String id;
    private String title;
    private final Games games;
    private final Images images;
    private final Links links;

    @Builder
    public GameResources(String id, String title, Games games, Images images, Links links) {
        this.id = id;
        this.title = title;
        this.games = games;
        this.images = images;
        this.links = links;
    }

    public void update(String title) {
        this.title = title;
    }
}
