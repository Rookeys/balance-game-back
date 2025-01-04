package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.GameResults;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "game_results")
public class GameResultsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "games_id")
    private GamesEntity games;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "images_id")
    private ImagesEntity images;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "links_id")
    private LinksEntity links;

    public static GameResultsEntity from(GameResults gameResults) {
        GameResultsEntity gameResultsEntity = new GameResultsEntity();
        gameResultsEntity.games = GamesEntity.from(gameResults.games());

        if (gameResults.images() != null) {
            gameResultsEntity.images = ImagesEntity.from(gameResults.images());
        }

        if (gameResults.links() != null) {
            gameResultsEntity.links = LinksEntity.from(gameResults.links());
        }

        return gameResultsEntity;
    }

    public GameResults toModel() {
        return GameResults.builder()
                .id(id)
                .games(games.toModel())
                .images(images != null ? images.toModel() : null)
                .links(links != null ? links.toModel() : null)
                .build();
    }
}

