package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.GamePlay;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Entity
@Table(name = "games_play")
public class GamePlayEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "games_id", nullable = false)
    private GamesEntity games;

    @Column(nullable = false)
    private int roundNumber;  // n강 선택

    @ElementCollection
    @CollectionTable(name = "round_resources", joinColumns = @JoinColumn(name = "games_play_id"))
    @Column(name = "resources_id")
    private List<Long> allResources; // 현재 게임에서 사용된 모든 리소스 ID

    @ElementCollection
    @CollectionTable(name = "selected_resources", joinColumns = @JoinColumn(name = "games_play_id"))
    @Column(name = "selected_resource_id")
    private List<Long> selectedResources; // 살아남은 리소스 ID

    @Column
    private boolean gameEnded = false;

    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    @Column
    @LastModifiedDate
    private LocalDateTime updatedDate;

    public static GamePlayEntity from(GamePlay gamePlay) {
        GamePlayEntity gamePlayEntity = new GamePlayEntity();
        gamePlayEntity.id = gamePlay.getId();
        gamePlayEntity.games = GamesEntity.from(gamePlay.getGames());
        gamePlayEntity.roundNumber = gamePlay.getRoundNumber();
        gamePlayEntity.allResources = gamePlay.getAllResources();
        gamePlayEntity.selectedResources = gamePlay.getSelectedResources();
        gamePlayEntity.gameEnded = gamePlay.isGameEnded();

        return gamePlayEntity;
    }

    public GamePlay toModel() {
        return GamePlay.builder()
                .id(id)
                .games(games.toModel())
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
