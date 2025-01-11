package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.infra.repository.game.GameResultJpaRepository;
import com.games.balancegameback.service.game.repository.GameResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GameResultRepositoryImpl implements GameResultRepository {

    private final GameResultJpaRepository gameResultJpaRepository;

    @Override
    public int countByGameId(Long roomId) {
        return gameResultJpaRepository.countByGameResourcesGamesId(roomId);
    }

    @Override
    public int countByGameResourcesId(Long resourcesId) {
        return gameResultJpaRepository.countByGameResourcesId(resourcesId);
    }
}
