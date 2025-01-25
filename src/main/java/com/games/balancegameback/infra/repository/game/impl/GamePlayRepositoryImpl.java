package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.domain.game.GamePlay;
import com.games.balancegameback.service.game.repository.GamePlayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GamePlayRepositoryImpl implements GamePlayRepository {


    @Override
    public void save(GamePlay gamePlay) {

    }

    @Override
    public void delete(GamePlay gamePlay) {

    }

    @Override
    public GamePlay findById(Long gamePlayId) {
        return null;
    }
}
