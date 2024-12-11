package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.GameResults;
import com.games.balancegameback.domain.media.Links;
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
    @JoinColumn(name = "media_id")
    private MediaEntity media;

    public static GameResultsEntity from(GameResults gameResults) {
        GameResultsEntity gameResultsEntity = new GameResultsEntity();
        gameResultsEntity.games = GamesEntity.from(gameResults.games());
        gameResultsEntity.media = MediaEntity.from(gameResults.media());

        return gameResultsEntity;
    }

    public GameResults toModel() {
        return GameResults.builder()
                .id(id)
                .games(games.toModel())
                .media(media.toModel())
                .build();
    }
}

