package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.GameInviteCode;
import com.games.balancegameback.domain.game.GameResources;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "games_id")
    private GamesEntity games;

    public static GameInviteCodeEntity from(GameInviteCode gameInviteCode) {
        GameInviteCodeEntity gameInviteCodeEntity = new GameInviteCodeEntity();
        gameInviteCodeEntity.inviteCode = gameInviteCode.inviteCode();
        gameInviteCodeEntity.isActive = gameInviteCode.isActive();
        gameInviteCodeEntity.games = GamesEntity.from(gameInviteCode.games());

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

