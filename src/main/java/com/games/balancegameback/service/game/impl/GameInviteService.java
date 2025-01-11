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

    public GameInviteCode createInviteCode(boolean isActive, String inviteCode) {
        GameInviteCode gameInviteCode = GameInviteCode.builder()
                .isActive(isActive)
                .inviteCode(inviteCode)
                .build();

        return gameInviteRepository.save(gameInviteCode);
    }

    public void updateInviteCode(String inviteCode, Games games) {
        GameInviteCode gameInviteCode = games.gameInviteCode();

        if (games.accessType().equals(AccessType.PROTECTED) && inviteCode != null) {
            gameInviteCode.setIsActive(true);
            gameInviteCode.setInviteCode(inviteCode);
        }

        if (!games.accessType().equals(AccessType.PROTECTED)) {
            gameInviteCode.setIsActive(false);
        }

        gameInviteRepository.save(gameInviteCode);
    }
}
