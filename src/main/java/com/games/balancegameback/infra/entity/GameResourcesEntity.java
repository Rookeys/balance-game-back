package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.GameResources;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "game_resources")
public class GameResourcesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    @Column
    @LastModifiedDate
    private LocalDateTime updatedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "games_id")
    private GamesEntity games;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "images_id")
    private ImagesEntity images;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "links_id")
    private LinksEntity links;

    public static GameResourcesEntity from(GameResources gameResources) {
        GameResourcesEntity gameResourcesEntity = new GameResourcesEntity();
        gameResourcesEntity.title = gameResources.getTitle();
        gameResourcesEntity.games = GamesEntity.from(gameResources.getGames());

        if (gameResources.getImages() != null) {
            gameResourcesEntity.images = ImagesEntity.from(gameResources.getImages());
        }

        if (gameResources.getLinks() != null) {
            gameResourcesEntity.links = LinksEntity.from(gameResources.getLinks());
        }

        return gameResourcesEntity;
    }

    public GameResources toModel() {
        return GameResources.builder()
                .id(id)
                .title(title)
                .games(games.toModel())
                .images(images != null ? images.toModel() : null)
                .links(links != null ? links.toModel() : null)
                .build();
    }
}

