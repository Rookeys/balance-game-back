package com.games.balancegameback.service.game.repository;

import com.games.balancegameback.domain.game.GameInviteCode;

public interface GameInviteRepository {

    GameInviteCode save(GameInviteCode gameInviteCode);

    void delete(GameInviteCode gameInviteCode);
}
