package com.games.balancegameback.service.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.BadRequestException;
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

        if (games.getAccessType().equals(AccessType.PROTECTED)) {
            if (inviteCode.isEmpty()) {
                throw new BadRequestException("해당 값은 비어 있을 수 없습니다.", ErrorCode.RUNTIME_EXCEPTION);
            }

            gameInviteCode.setInviteCode(inviteCode);
        } else {
            if (inviteCode.isEmpty()) {
                gameInviteCode.setIsActive(false);
            } else {
                gameInviteCode.setIsActive(false);
                gameInviteCode.setInviteCode(inviteCode);
            }
        }

        gameInviteRepository.update(gameInviteCode);
    }
}
