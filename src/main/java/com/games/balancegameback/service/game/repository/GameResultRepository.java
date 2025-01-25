package com.games.balancegameback.service.game.repository;

import com.games.balancegameback.domain.game.GameResults;

public interface GameResultRepository {

    int countByGameId(Long roomId);

    int countByGameResourcesId(Long resourcesId);

    void save(GameResults gameResults);
}
