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

    public void updateInviteCode(String inviteCode, Games games) {
        GameInviteCode gameInviteCode = gameInviteRepository.findById(games.getGameInviteCode().getId());

        if (games.getAccessType().equals(AccessType.PROTECTED) && inviteCode != null) {
            gameInviteCode.setIsActive(true);
            gameInviteCode.setInviteCode(inviteCode);
        }

        if (!games.getAccessType().equals(AccessType.PROTECTED)) {
            gameInviteCode.setIsActive(false);
        }

        gameInviteRepository.update(gameInviteCode);
    }
}
