package com.games.balancegameback.service.game.impl;

import com.games.balancegameback.domain.game.GameInviteCode;
import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.game.enums.AccessType;
import com.games.balancegameback.service.game.repository.GameInviteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GameInviteService {

    private final GameInviteRepository gameInviteRepository;

    public void createInviteCode(String inviteCode, Games games) {
        GameInviteCode gameInviteCode = GameInviteCode.builder()
                .isActive(true)
                .inviteCode(inviteCode)
                .games(games)
                .build();

        gameInviteRepository.save(gameInviteCode);
    }

    public void updateInviteCode(String inviteCode, Games games) {
        GameInviteCode gameInviteCode = gameInviteRepository.findByGamesId(games.id());

        if (games.accessType().equals(AccessType.PROTECTED) && inviteCode != null) {
            gameInviteCode.setIsActive(true);
            gameInviteCode.setInviteCode(inviteCode);
        }

        if (!games.accessType().equals(AccessType.PROTECTED)) {
            gameInviteCode.setIsActive(false);
        }

        gameInviteRepository.save(gameInviteCode);
    }

    public void deleteInviteCode(Long roomId) {
        GameInviteCode gameInviteCode = gameInviteRepository.findByGamesId(roomId);
        gameInviteRepository.delete(gameInviteCode);
    }
}
