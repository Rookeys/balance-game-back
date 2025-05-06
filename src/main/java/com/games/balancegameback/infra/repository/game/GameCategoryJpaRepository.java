package com.games.balancegameback.infra.repository.game;

import com.games.balancegameback.infra.entity.GameCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameCategoryJpaRepository extends JpaRepository<GameCategoryEntity, Long> {

    void deleteByGamesId(Long gamesId);
}
