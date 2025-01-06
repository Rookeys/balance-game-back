package com.games.balancegameback.service.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.BadRequestException;
import com.games.balancegameback.core.exception.impl.UnAuthorizedException;
import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.game.enums.AccessType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.GameListResponse;
import com.games.balancegameback.dto.game.GameRequest;
import com.games.balancegameback.dto.game.GameResponse;
import com.games.balancegameback.service.game.repository.GameRepository;
import com.games.balancegameback.service.user.impl.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameRoomService {

    private final GameRepository gameRepository;
    private final UserUtils userUtils;

    public void saveGame(GameRequest gameRequest, HttpServletRequest request) {
        if (gameRequest.getAccessType().equals(AccessType.PROTECTED) && gameRequest.getInviteCode() == null) {
            throw new BadRequestException("초대 코드가 null 입니다.", ErrorCode.INVITE_CODE_NULL_EXCEPTION);
        }

        Users users = userUtils.findUserByToken(request);
        Games games = Games.builder()
                .title(gameRequest.getTitle())
                .description(gameRequest.getDescription())
                .accessType(gameRequest.getAccessType())
                .category(gameRequest.getCategory())
                .isNamePublic(gameRequest.isNamePublic())
                .users(users)
                .build();

        gameRepository.save(games);
        // inviteCode 저장 로직 추가 예정.
    }

    public GameResponse getGameStatus(Long roomId, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        this.existsHost(roomId, users);

        return gameRepository.findById(roomId);
    }

    public Page<GameListResponse> getMyGameList(Pageable pageable, Long cursorId, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        return gameRepository.findGamesWithResources(cursorId, users, pageable);
    }

    public void updateGameStatus(Long roomId, GameRequest gameRequest, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        this.existsHost(roomId, users);

        if (gameRequest.getAccessType().equals(AccessType.PROTECTED)) {
            // inviteCode 가 수정되었는지 확인 후 수정되었다면 업데이트
        }

        Games games = Games.builder()
                .id(roomId)
                .title(gameRequest.getTitle())
                .description(gameRequest.getDescription())
                .isNamePublic(gameRequest.isNamePublic())
                .accessType(gameRequest.getAccessType())
                .category(gameRequest.getCategory())
                .build();

        gameRepository.update(games);
    }

    public void deleteGame(Long roomId, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        this.existsHost(roomId, users);

        gameRepository.deleteById(roomId);
        // 트리거에 해당 로직이 실행되었을 때 리소스들 중 연결되어 있는 방이 없으면 삭제하는 로직 추가 예정.
    }

    private void existsHost(Long roomId, Users users) {
        if (!gameRepository.existsByIdAndUsers(roomId, users)) {
            throw new UnAuthorizedException("게임 주인이 아닙니다.", ErrorCode.NOT_ALLOW_WRITE_EXCEPTION);
        }
    }
}
