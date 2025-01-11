package com.games.balancegameback.domain.game;

import lombok.Builder;
import lombok.Data;

@Data
public class GameInviteCode {

    private Long id;
    private String inviteCode;
    private Boolean isActive;
    private Games games;

    @Builder
    public GameInviteCode(Long id, String inviteCode, Boolean isActive, Games games) {
        this.id = id;
        this.inviteCode = inviteCode;
        this.isActive = isActive;
        this.games = games;
    }
}
