package com.games.balancegameback.infra.repository.game;

import com.games.balancegameback.domain.game.GameInviteCode;
import com.games.balancegameback.infra.entity.GameInviteCodeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GameInviteJpaRepository extends JpaRepository<GameInviteCodeEntity, Long> {

    GameInviteCode findByGamesId(Long roomId);
}
