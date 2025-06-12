package com.games.balancegameback.infra.repository.game;

import com.games.balancegameback.infra.entity.GameCategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameCategoryJpaRepository extends JpaRepository<GameCategoryEntity, String> {

    void deleteByGameId(String gameId);

    List<GameCategoryEntity> findByGameId(String gameId);
}
