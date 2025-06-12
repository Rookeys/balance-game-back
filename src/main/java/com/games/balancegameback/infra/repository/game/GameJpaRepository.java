package com.games.balancegameback.infra.repository.game;

import com.games.balancegameback.infra.entity.GamesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameJpaRepository extends JpaRepository<GamesEntity, String> {

    List<GamesEntity> findByUserId(String userId);

    void deleteByUserId(String userId);

    @Query("SELECT g FROM GamesEntity g WHERE g.userId = :userId")
    List<GamesEntity> findGamesByUserId(@Param("userId") String userId);
}
