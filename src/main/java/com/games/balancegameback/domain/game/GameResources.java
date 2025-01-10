package com.games.balancegameback.domain.game;

import com.games.balancegameback.domain.media.Images;
import com.games.balancegameback.domain.media.Links;
import lombok.Builder;
import lombok.Getter;

@Getter
public class GameResources {

    private final Long id;
    private String title;
    private final Games games;
    private Links links;
    private Images images;

    @Builder
    public GameResources(Long id, String title, Games games, Links links, Images images) {
        this.id = id;
        this.title = title;
        this.games = games;
        this.links = links;
        this.images = images;
    }

    public void updateImage(String title, Images images) {
        this.title = title;
        this.images = images;
    }

    public void updateLinks(String title, Links links) {
        this.title = title;
        this.links = links;
    }
}
