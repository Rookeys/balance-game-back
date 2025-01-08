package com.games.balancegameback.service.game.repository;

import com.games.balancegameback.domain.game.GameInviteCode;

public interface GameInviteRepository {

    void save(GameInviteCode gameInviteCode);

    GameInviteCode findByGamesId(Long roomId);

    void delete(GameInviteCode gameInviteCode);
}
