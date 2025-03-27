package com.games.balancegameback.service.game.repository;

import com.games.balancegameback.domain.game.GameCategory;

public interface GameCategoryRepository {

    void update(GameCategory gameCategory);

    void deleteAll(Long gameId);
}
