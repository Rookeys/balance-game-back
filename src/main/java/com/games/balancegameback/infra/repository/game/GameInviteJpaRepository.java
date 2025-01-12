package com.games.balancegameback.infra.repository.game;

import com.games.balancegameback.infra.entity.GameInviteCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameInviteJpaRepository extends JpaRepository<GameInviteCodeEntity, Long> {

    GameInviteCodeEntity findByGamesId(Long roomId);
}
