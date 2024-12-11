package com.games.balancegameback.domain.game;

import lombok.Builder;

public record GameInviteCode(Long id, String inviteCode, Boolean isActive, Games games) {

    @Builder
    public GameInviteCode {

    }
}
