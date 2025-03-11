package com.games.balancegameback.infra.repository.game;

import com.games.balancegameback.infra.entity.GameResourcesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameResourceJpaRepository extends JpaRepository<GameResourcesEntity, Long> {

    Integer countByGamesId(Long gameId);
}
