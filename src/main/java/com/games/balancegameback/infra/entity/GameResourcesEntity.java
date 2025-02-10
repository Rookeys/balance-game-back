package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.GameResources;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "game_resources")
public class GameResourcesEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "games_id", nullable = false)
    private GamesEntity games;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "images_id")
    private ImagesEntity images;

    @ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "links_id")
    private LinksEntity links;

    public static GameResourcesEntity from(GameResources gameResources) {
        GameResourcesEntity gameResourcesEntity = new GameResourcesEntity();
        gameResourcesEntity.id = gameResources.getId();
        gameResourcesEntity.title = gameResources.getTitle() != null ? gameResources.getTitle() : "";
        gameResourcesEntity.games = GamesEntity.from(gameResources.getGames());

        if (gameResources.getImages() != null) {
            gameResourcesEntity.images = ImagesEntity.from(gameResources.getImages());
        }

        if (gameResources.getLinks() != null) {
            gameResourcesEntity.links = LinksEntity.from((gameResources.getLinks()));
        }

        return gameResourcesEntity;
    }

    public GameResources toModel() {
        return GameResources.builder()
                .id(id)
                .title(title != null ? title : "")
                .games(games.toModel())
                .images(images != null ? images.toModel() : null)
                .links(links != null ? links.toModel() : null)
                .build();
    }

    public void update(GameResources gameResources) {
        this.title = gameResources.getTitle();
        this.images = gameResources.getImages() != null ? ImagesEntity.from(gameResources.getImages()) : null;
        this.links = gameResources.getLinks() != null ? LinksEntity.from(gameResources.getLinks()) : null;
    }
}

