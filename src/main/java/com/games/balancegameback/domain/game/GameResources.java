package com.games.balancegameback.domain.game;

import com.games.balancegameback.domain.media.Images;
import com.games.balancegameback.domain.media.Links;
import com.games.balancegameback.infra.entity.GameResultsEntity;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class GameResources {

    private final Long id;
    private String title;
    private final Games games;
    private final Images images;
    private final Links links;
    private final List<GameResults> winningLists;

    @Builder
    public GameResources(Long id, String title, Games games, Images images, Links links, List<GameResults> winningLists) {
        this.id = id;
        this.title = title;
        this.games = games;
        this.images = images;
        this.links = links;
        this.winningLists = winningLists;
    }

    public void update(String title) {
        this.title = title;
    }
}
