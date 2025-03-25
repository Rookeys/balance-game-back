package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.domain.game.GameCategory;
import com.games.balancegameback.infra.entity.GameCategoryEntity;
import com.games.balancegameback.infra.repository.game.GameCategoryJpaRepository;
import com.games.balancegameback.service.game.repository.GameCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GameCategoryRepositoryImpl implements GameCategoryRepository {

    private final GameCategoryJpaRepository gameCategoryRepository;

    @Override
    public void update(GameCategory gameCategory) {
        gameCategoryRepository.save(GameCategoryEntity.from(gameCategory));
    }

    @Override
    public void deleteAll(Long gameId) {
        gameCategoryRepository.deleteByGamesId(gameId);
    }
}
