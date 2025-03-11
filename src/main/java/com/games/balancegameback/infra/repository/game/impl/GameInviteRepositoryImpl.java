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
    public GameInviteCode save(GameInviteCode gameInviteCode) {
        GameInviteCodeEntity entity = gameRepository.save(GameInviteCodeEntity.from(gameInviteCode));
        return entity.toModel();
    }

    @Override
    public void update(GameInviteCode gameInviteCode) {
        GameInviteCodeEntity entity = gameRepository.findById(gameInviteCode.getId()).orElseThrow();
        entity.update(gameInviteCode);
    }

    @Override
    public GameInviteCode findById(Long id) {
        GameInviteCodeEntity entity = gameRepository.findById(id).orElseThrow();
        return entity.toModel();
    }

    @Override
    public GameInviteCode findByGameId(Long roomId) {
        GameInviteCodeEntity entity = gameRepository.findByGamesId(roomId);
        return entity.toModel();
    }

    @Override
    public void delete(GameInviteCode gameInviteCode) {
        gameRepository.delete(GameInviteCodeEntity.from(gameInviteCode));
    }
}
