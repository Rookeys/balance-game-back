package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.GameResults;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "game_results", indexes = {
        @Index(name = "idx_game_results_resource_id", columnList = "game_resource_id")
})
public class GameResultsEntity extends BaseEntity {

    @Column(name = "game_resource_id", nullable = false, length = 36)
    private String gameResourceId;

    @Override
    protected String getEntityPrefix() {
        return "GRT";
    }

    @PrePersist
    public void prePersist() {
        generateId();
    }

    public static GameResultsEntity from(GameResults gameResults) {
        GameResultsEntity entity = new GameResultsEntity();
        entity.id = gameResults.id();
        entity.gameResourceId = gameResults.gameResources().getId();
        return entity;
    }

    public GameResults toModel() {
        return GameResults.builder()
                .id(id)
                .build();
    }
}

