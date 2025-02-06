package com.games.balancegameback.service.game;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.UnAuthorizedException;
import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.media.Images;
import com.games.balancegameback.domain.media.Links;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.*;
import com.games.balancegameback.dto.game.gameplay.GamePlayRequest;
import com.games.balancegameback.dto.game.gameplay.GamePlayResponse;
import com.games.balancegameback.dto.game.gameplay.GamePlayRoundRequest;
import com.games.balancegameback.dto.media.ImageRequest;
import com.games.balancegameback.dto.media.LinkRequest;
import com.games.balancegameback.service.game.impl.GamePlayService;
import com.games.balancegameback.service.game.impl.GameResourceService;
import com.games.balancegameback.service.game.impl.GameResultService;
import com.games.balancegameback.service.game.impl.GameRoomService;
import com.games.balancegameback.service.game.repository.GameRepository;
import com.games.balancegameback.service.user.impl.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRoomService gameRoomService;
    private final GameResourceService gameResourceService;
    private final GamePlayService gamePlayService;
    private final GameResultService gameResultService;
    private final GameRepository gameRepository;
    private final UserUtils userUtils;

    // 게임방 생성
    public Long saveGame(GameRequest gameRequest, HttpServletRequest request) {
        return gameRoomService.saveGame(gameRequest, request);
    }

    // 게임 설정값 반환
    public GameResponse getGameStatus(Long gameId, HttpServletRequest request) {
        return gameRoomService.getGameStatus(gameId, request);
    }

    // 내가 만든 게임들 리스트 반환
    public Page<GameListResponse> getMyGameList(Pageable pageable, Long cursorId, HttpServletRequest request) {
        return gameRoomService.getMyGameList(pageable, cursorId, request);
    }

    // 게임방 설정 업데이트
    public void updateGameStatus(Long gameId, GameRequest gameRequest, HttpServletRequest request) {
        gameRoomService.updateGameStatus(gameId, gameRequest, request);
    }

    // 게임방 삭제
    public void deleteGame(Long gameId, HttpServletRequest request) {
        gameRoomService.deleteGame(gameId, request);
    }

    // 게임 리소스에 유튜브 링크 추가
    public void saveLinkResource(Games games, LinkRequest linkRequest) {
        gameResourceService.saveLinkResource(games, linkRequest);
    }

    // 게임 리소스에 이미지 추가
    public void saveImageResource(Games games, ImageRequest imageRequest) {
        gameResourceService.saveImageResource(games, imageRequest);
    }

    // 등록된 리소스 목록을 반환
    public Page<GameResourceResponse> getResources(Long gameId, Long cursorId, Pageable pageable,
                                                   GameResourceSearchRequest gameResourceSearchRequest,
                                                   HttpServletRequest request) {
        this.validateRequest(gameId, request);
        return gameResourceService.getResources(gameId, cursorId, pageable, gameResourceSearchRequest);
    }

    // 등록한 리소스의 정보를 수정함
    public void updateResource(Long roomId, Long resourceId, GameResourceRequest gameResourceRequest,
                               HttpServletRequest request) {
        this.validateRequest(roomId, request);
        gameResourceService.updateResource(resourceId, gameResourceRequest);
    }

    // 게임방 생성 및 게임 시작
    public GamePlayResponse createPlayRoom(Long gameId, GamePlayRoundRequest request) {
        return gamePlayService.createPlayRoom(gameId, request);
    }

    // 게임방 생성 및 게임 시작
    public GamePlayResponse updatePlayRoom(Long gameId, Long playId, GamePlayRequest request) {
        return gamePlayService.updatePlayRoom(gameId, playId, request);
    }

    // 리소스를 삭제함
    public void deleteResource(Long roomId, Long resourceId, HttpServletRequest request) {
        this.validateRequest(roomId, request);
        gameResourceService.deleteResource(resourceId);
    }

    // 게임 결과창 출력
    public Page<GameResultResponse> getResultRanking(Long gameId, Long cursorId,
                                                     GameResourceSearchRequest request,
                                                     Pageable pageable) {
        return gameResultService.getResultRanking(gameId, cursorId, request, pageable);
    }

    // api 요청한 유저가 해당 게임방 주인이 맞는지 확인.
    private void validateRequest(Long roomId, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);

        if (!gameRepository.existsByIdAndUsers(roomId, users)) {
            throw new UnAuthorizedException("정보가 일치하지 않습니다.", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }
    }
}
