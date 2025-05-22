package com.games.balancegameback.infra.repository.game;

import com.games.balancegameback.infra.entity.GamePlayEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GamePlayJpaRepository extends JpaRepository<GamePlayEntity, Long> {

    void deleteByGamesId(Long gameId);
}
