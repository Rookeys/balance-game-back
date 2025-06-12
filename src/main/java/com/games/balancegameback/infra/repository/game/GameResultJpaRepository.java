package com.games.balancegameback.infra.repository.game;

import com.games.balancegameback.infra.entity.GameResultsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GameResultJpaRepository extends JpaRepository<GameResultsEntity, String> {

    @Query("SELECT COUNT(gr) FROM GameResultsEntity gr JOIN GameResourcesEntity grs ON gr.gameResourceId = grs.id WHERE grs.gameId = :gameId")
    int countByGameId(@Param("gameId") String gameId);

    @Query("SELECT COUNT(gr) FROM GameResultsEntity gr WHERE gr.gameResourceId = :resourceId")
    int countByResourceId(@Param("resourceId") String resourceId);
}
