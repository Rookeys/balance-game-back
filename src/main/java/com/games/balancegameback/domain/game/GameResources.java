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
    private Images images;
    private Links links;

    @Builder
    public GameResources(Long id, String title, Games games, Images images, Links links) {
        this.id = id;
        this.title = title;
        this.games = games;
        this.images = images;
        this.links = links;
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
