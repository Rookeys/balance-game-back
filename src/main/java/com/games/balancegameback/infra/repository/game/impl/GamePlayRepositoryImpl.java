package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.domain.game.GamePlay;
import com.games.balancegameback.infra.entity.GamePlayEntity;
import com.games.balancegameback.infra.entity.GamesEntity;
import com.games.balancegameback.infra.repository.game.GamePlayJpaRepository;
import com.games.balancegameback.service.game.repository.GamePlayRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GamePlayRepositoryImpl implements GamePlayRepository {

    private final GamePlayJpaRepository gamePlayRepository;

    @Override
    public GamePlay save(GamePlay gamePlay) {
        GamePlayEntity entity = gamePlayRepository.save(GamePlayEntity.from(gamePlay));
        return entity.toModel();
    }

    @Override
    public void update(GamePlay gamePlay) {
        GamePlayEntity entity = gamePlayRepository.findById(gamePlay.getId())
                .orElseThrow(() -> new NotFoundException("게임 플레이 정보를 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

        entity.update(gamePlay);
    }

    @Override
    public void delete(GamePlay gamePlay) {
        gamePlayRepository.delete(GamePlayEntity.from(gamePlay));
    }

    @Override
    public GamePlay findById(Long gamePlayId) {
        return gamePlayRepository.findById(gamePlayId).orElseThrow(() ->
                new NotFoundException("없는 플레이룸입니다.", ErrorCode.NOT_FOUND_EXCEPTION))
                .toModel();
    }
}
