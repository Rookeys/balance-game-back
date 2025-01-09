package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.domain.game.GameInviteCode;
import com.games.balancegameback.infra.entity.GameInviteCodeEntity;
import com.games.balancegameback.infra.repository.game.GameInviteJpaRepository;
import com.games.balancegameback.service.game.repository.GameInviteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GameInviteRepositoryImpl implements GameInviteRepository {

    private final GameInviteJpaRepository gameRepository;

    @Override
    public void save(GameInviteCode gameInviteCode) {
        gameRepository.save(GameInviteCodeEntity.from(gameInviteCode));
    }

    @Override
    public GameInviteCode findByGamesId(Long roomId) {
        return gameRepository.findByGamesId(roomId).toModel();
    }

    @Override
    public void delete(GameInviteCode gameInviteCode) {
        gameRepository.delete(GameInviteCodeEntity.from(gameInviteCode));
    }
}
