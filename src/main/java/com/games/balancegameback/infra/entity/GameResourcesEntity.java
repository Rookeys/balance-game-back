package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.GameResources;
import com.games.balancegameback.domain.game.GameResults;
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
    @JoinColumn(name = "media_id")
    private MediaEntity media;

    public static GameResourcesEntity from(GameResources gameResources) {
        GameResourcesEntity gameResourcesEntity = new GameResourcesEntity();
        gameResourcesEntity.title = gameResources.title();
        gameResourcesEntity.games = GamesEntity.from(gameResources.games());
        gameResourcesEntity.media = MediaEntity.from(gameResources.media());

        return gameResourcesEntity;
    }

    public GameResources toModel() {
        return GameResources.builder()
                .id(id)
                .title(title)
                .games(games.toModel())
                .media(media.toModel())
                .build();
    }
}

