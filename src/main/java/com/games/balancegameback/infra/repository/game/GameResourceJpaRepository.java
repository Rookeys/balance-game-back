package com.games.balancegameback.infra.repository.game;

import com.games.balancegameback.infra.entity.GameResourcesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GameResourceJpaRepository extends JpaRepository<GameResourcesEntity, String> {

    @Query("SELECT COUNT(gr) FROM GameResourcesEntity gr WHERE gr.gameId = :gameId")
    Long countByGameId(@Param("gameId") String gameId);

    void deleteByGameId(String gameId);
}
