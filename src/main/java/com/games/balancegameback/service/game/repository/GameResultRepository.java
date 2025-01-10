package com.games.balancegameback.service.game.repository;

public interface GameResultRepository {

    int countByGameId(Long roomId);

    int countByGameResourcesId(Long resourcesId);
}
