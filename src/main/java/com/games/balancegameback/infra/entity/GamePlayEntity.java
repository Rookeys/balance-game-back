package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.GamePlay;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

@Getter
@Entity
@Table(name = "games_play", indexes = {
        @Index(name = "idx_games_play_game_id", columnList = "game_id")
})
public class GamePlayEntity extends BaseEntity {

    @Column(name = "game_id", nullable = false, length = 36)
    private String gameId;

    @Column(nullable = false)
    private int roundNumber;

    @ElementCollection
    @CollectionTable(name = "round_resources", joinColumns = @JoinColumn(name = "games_play_id"))
    @Column(name = "resources_id")
    private List<String> allResources;

    @ElementCollection
    @CollectionTable(name = "selected_resources", joinColumns = @JoinColumn(name = "games_play_id"))
    @Column(name = "selected_resource_id")
    private List<String> selectedResources;

    @Column
    private boolean gameEnded = false;

    @Override
    protected String getEntityPrefix() {
        return "GPL";
    }

    @PrePersist
    public void prePersist() {
        generateId();
    }

    public static GamePlayEntity from(GamePlay gamePlay) {
        GamePlayEntity entity = new GamePlayEntity();
        entity.id = gamePlay.getId();
        entity.gameId = gamePlay.getGames().getId();
        entity.roundNumber = gamePlay.getRoundNumber();
        entity.allResources = gamePlay.getAllResources();
        entity.selectedResources = gamePlay.getSelectedResources();
        entity.gameEnded = gamePlay.isGameEnded();
        return entity;
    }

    public GamePlay toModel() {
        return GamePlay.builder()
                .id(id)
                .roundNumber(roundNumber)
                .allResources(allResources)
                .selectedResources(selectedResources)
                .gameEnded(gameEnded)
                .build();
    }

    public void update(GamePlay gamePlay) {
        this.allResources = gamePlay.getAllResources();
        this.selectedResources = gamePlay.getSelectedResources();
        this.gameEnded = gamePlay.isGameEnded();
        this.roundNumber = gamePlay.getRoundNumber();
    }
}
