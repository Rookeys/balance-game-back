package com.games.balancegameback.infra.repository.game;

import com.games.balancegameback.infra.entity.GameResultsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameResultJpaRepository extends JpaRepository<GameResultsEntity, Long> {

    int countByGameResourcesGamesId(Long roomId);

    int countByGameResourcesId(Long resourceId);
}
