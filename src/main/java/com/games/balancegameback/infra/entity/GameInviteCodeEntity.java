package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.GameInviteCode;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "game_invite_code")
public class GameInviteCodeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String inviteCode;

    @Column
    private Boolean isActive = true;

    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    @Column
    @LastModifiedDate
    private LocalDateTime updatedDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "games_id")
    private GamesEntity games;

    public static GameInviteCodeEntity from(GameInviteCode gameInviteCode) {
        GameInviteCodeEntity gameInviteCodeEntity = new GameInviteCodeEntity();
        gameInviteCodeEntity.inviteCode = gameInviteCode.getInviteCode();
        gameInviteCodeEntity.isActive = gameInviteCode.getIsActive();
        gameInviteCodeEntity.games = GamesEntity.from(gameInviteCode.getGames());

        return gameInviteCodeEntity;
    }

    public GameInviteCode toModel() {
        return GameInviteCode.builder()
                .id(id)
                .inviteCode(inviteCode)
                .isActive(isActive)
                .games(games.toModel())
                .build();
    }
}

