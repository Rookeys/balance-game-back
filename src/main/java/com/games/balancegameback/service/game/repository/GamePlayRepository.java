package com.games.balancegameback.service.game.repository;

import com.games.balancegameback.domain.game.GamePlay;

public interface GamePlayRepository {


    void save(GamePlay gamePlay);

    void delete(GamePlay gamePlay);

    GamePlay findById(Long gamePlayId);
}
