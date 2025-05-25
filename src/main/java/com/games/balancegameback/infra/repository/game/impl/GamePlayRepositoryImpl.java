package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.domain.game.GamePlay;
import com.games.balancegameback.domain.game.enums.AccessType;
import com.games.balancegameback.infra.entity.GamePlayEntity;
import com.games.balancegameback.infra.entity.QGamePlayEntity;
import com.games.balancegameback.infra.entity.QGamesEntity;
import com.games.balancegameback.infra.repository.game.GamePlayJpaRepository;
import com.games.balancegameback.service.game.repository.GamePlayRepository;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Random;

@Repository
@RequiredArgsConstructor
public class GamePlayRepositoryImpl implements GamePlayRepository {

    private final GamePlayJpaRepository gamePlayRepository;
    private final JPAQueryFactory jpaQueryFactory;

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

    @Override
    public Long findRandomGamePlayId() {
        QGamesEntity games = QGamesEntity.gamesEntity;

        List<Long> ids = jpaQueryFactory
                .select(games.id)
                .from(games)
                .where(games.accessType.eq(AccessType.PUBLIC))
                .groupBy(games.id)
                .having(games.gameResources.size().goe(2))
                .fetch();

        if (ids.isEmpty()) {
            return null;
        }

        int randomIndex = new Random().nextInt(ids.size());
        return ids.get(randomIndex);
    }
}
