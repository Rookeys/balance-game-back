package com.games.balancegameback.service.game.repository;

import com.games.balancegameback.domain.game.GamePlay;

public interface GamePlayRepository {

    GamePlay save(GamePlay gamePlay);

    void update(GamePlay gamePlay);

    void delete(GamePlay gamePlay);

    GamePlay findById(Long gamePlayId);
}
