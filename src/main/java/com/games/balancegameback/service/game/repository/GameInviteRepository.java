package com.games.balancegameback.service.game.repository;

import com.games.balancegameback.domain.game.GameInviteCode;

public interface GameInviteRepository {

    GameInviteCode save(GameInviteCode gameInviteCode);

    void update(GameInviteCode gameInviteCode);

    GameInviteCode findById(Long id);

    GameInviteCode findByGameId(Long roomId);

    void delete(GameInviteCode gameInviteCode);
}
