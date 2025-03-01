package com.games.balancegameback.service.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.BadRequestException;
import com.games.balancegameback.core.exception.impl.UnAuthorizedException;
import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.GameInviteCode;
import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.game.enums.AccessType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.*;
import com.games.balancegameback.service.game.repository.GameRepository;
import com.games.balancegameback.service.user.impl.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameRoomService {

    private final GameRepository gameRepository;
    private final GameInviteService gameInviteService;
    private final UserUtils userUtils;

    @Transactional
    public Long saveGame(GameRequest gameRequest, HttpServletRequest request) {
        if (gameRequest.getAccessType().equals(AccessType.PROTECTED) && gameRequest.getInviteCode() == null) {
            throw new BadRequestException("초대 코드가 null 입니다.", ErrorCode.INVITE_CODE_NULL_EXCEPTION);
        }

        Users users = userUtils.findUserByToken(request);

        // 게임방 생성
        Games games = Games.builder()
                .title(gameRequest.getTitle())
                .description(gameRequest.getDescription())
                .accessType(gameRequest.getAccessType())
                .category(gameRequest.getCategory())
                .isNamePublic(gameRequest.isNamePublic())
                .users(users)
                .build();

        // 초대 코드 생성 및 관계 설정
        GameInviteCode gameInviteCode = GameInviteCode.builder()
                .inviteCode(gameRequest.getInviteCode() != null ? gameRequest.getInviteCode() : "")
                .isActive(gameRequest.getAccessType().equals(AccessType.PROTECTED))
                .games(games)
                .build();

        // 양방향 관계 설정
        games.setGameInviteCode(gameInviteCode);

        return gameRepository.save(games).getId();
    }

    public GameResponse getGameStatus(Long gameId, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        this.existsHost(gameId, users);

        return gameRepository.findById(gameId);
    }

    public CustomPageImpl<GameListResponse> getMyGameList(Pageable pageable, Long cursorId,
                                                          GameSearchRequest searchRequest,
                                                          HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        return gameRepository.findGamesWithResources(cursorId, users, pageable, searchRequest);
    }

    @Transactional
    public void updateGameStatus(Long gameId, GameRequest gameRequest, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        this.existsHost(gameId, users);

        Games games = gameRepository.findByRoomId(gameId);
        games.update(gameRequest);

        gameInviteService.updateInviteCode(gameRequest.getInviteCode(), games);
        gameRepository.update(games);
    }

    @Transactional
    public void deleteGame(Long gameId, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        this.existsHost(gameId, users);

        gameRepository.deleteById(gameId);
        // 트리거에 해당 로직이 실행되었을 때 리소스들 중 연결되어 있는 방이 없으면 삭제하는 로직 추가 예정.
    }

    private void existsHost(Long gameId, Users users) {
        if (!gameRepository.existsByIdAndUsers(gameId, users)) {
            throw new UnAuthorizedException("게임 주인이 아닙니다.", ErrorCode.NOT_ALLOW_WRITE_EXCEPTION);
        }
    }
}
