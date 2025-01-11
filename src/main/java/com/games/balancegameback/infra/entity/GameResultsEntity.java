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
    @JoinColumn(name = "game_resources_id")
    private GameResourcesEntity gameResources;

    public static GameResultsEntity from(GameResults gameResults) {
        GameResultsEntity gameResultsEntity = new GameResultsEntity();
        gameResultsEntity.gameResources = GameResourcesEntity.from(gameResults.gameResources());

        return gameResultsEntity;
    }

    public GameResults toModel() {
        return GameResults.builder()
                .id(id)
                .gameResources(gameResources.toModel())
                .build();
    }
}

