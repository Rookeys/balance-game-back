package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.GameInviteCode;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Entity
@Table(name = "game_invite_code")
public class GameInviteCodeEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String inviteCode;

    @Column
    private Boolean isActive = true;

    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "games_id", nullable = false)
    private GamesEntity games;

    public static GameInviteCodeEntity from(GameInviteCode gameInviteCode) {
        if (gameInviteCode == null) return null;

        GameInviteCodeEntity gameInviteCodeEntity = new GameInviteCodeEntity();
        gameInviteCodeEntity.inviteCode = gameInviteCode.getInviteCode();
        gameInviteCodeEntity.isActive = gameInviteCode.getIsActive();
        gameInviteCodeEntity.games = null;

        return gameInviteCodeEntity;
    }

    public GameInviteCode toModel() {
        return GameInviteCode.builder()
                .id(id)
                .inviteCode(inviteCode)
                .isActive(isActive)
                .games(null)
                .build();
    }

    public void update(GameInviteCode gameInviteCode) {
        this.inviteCode = gameInviteCode.getInviteCode();
        this.isActive = gameInviteCode.getIsActive();
    }
}

