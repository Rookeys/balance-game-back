package com.games.balancegameback.infra.repository.game;

import com.games.balancegameback.infra.entity.GamesEntity;
import com.games.balancegameback.infra.entity.UsersEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameJpaRepository extends JpaRepository<GamesEntity, Long> {

}
