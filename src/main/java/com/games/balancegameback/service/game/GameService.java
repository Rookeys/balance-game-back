package com.games.balancegameback.service.game;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.UnAuthorizedException;
import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.game.enums.CommentType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.*;
import com.games.balancegameback.dto.game.comment.*;
import com.games.balancegameback.dto.game.gameplay.GameInfoResponse;
import com.games.balancegameback.dto.game.gameplay.GamePlayRequest;
import com.games.balancegameback.dto.game.gameplay.GamePlayResponse;
import com.games.balancegameback.dto.game.gameplay.GamePlayRoundRequest;
import com.games.balancegameback.dto.media.ImageRequest;
import com.games.balancegameback.dto.media.LinkRequest;
import com.games.balancegameback.service.game.impl.*;
import com.games.balancegameback.service.game.impl.comment.GameCommentLikesService;
import com.games.balancegameback.service.game.impl.comment.GameResourceCommentService;
import com.games.balancegameback.service.game.impl.comment.GameResultCommentService;
import com.games.balancegameback.service.game.repository.GameRepository;
import com.games.balancegameback.service.user.impl.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameListService gameListService;
    private final GameRoomService gameRoomService;
    private final GameResourceService gameResourceService;
    private final GamePlayService gamePlayService;
    private final GameResultService gameResultService;
    private final GameResourceCommentService gameResourceCommentService;
    private final GameResultCommentService gameResultCommentService;
    private final GameCommentLikesService gameCommentLikesService;
    private final GameRepository gameRepository;
    private final UserUtils userUtils;

    // 메인 페이지 출력
    public CustomPageImpl<GameListResponse> getMainGameList(Long cursorId, Pageable pageable,
                                                            GameSearchRequest searchRequest) {
        return gameListService.getMainGameList(cursorId, pageable, searchRequest);
    }

    // 각 카테고리 별 게임 수 출력
    public GameCategoryNumsResponse getCategoryNums(String title) {
        return gameListService.getCategoryNums(title);
    }

    // 게임 설정값 반환
    public GameDetailResponse getGameStatus(Long gameId) {
        return gameListService.getGameStatus(gameId);
    }

    // 게임방 생성
    public Long saveGame(GameRequest gameRequest, HttpServletRequest request) {
        return gameRoomService.saveGame(gameRequest, request);
    }

    // 해당 게임방 내 리소스 총 갯수 반환
    public Integer getCountResourcesInGames(Long gameId) {
        return gameResourceService.getCountResourcesInGames(gameId);
    }

    // 내가 만든 게임방 설정값 반환
    public GameResponse getMyGameStatus(Long gameId, HttpServletRequest request) {
        return gameRoomService.getMyGameStatus(gameId, request);
    }

    // 내가 만든 게임들 리스트 반환
    public CustomPageImpl<GameListResponse> getMyGameList(Pageable pageable, Long cursorId,
                                                GameSearchRequest searchRequest,
                                                HttpServletRequest request) {
        return gameRoomService.getMyGameList(pageable, cursorId, searchRequest, request);
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

    // 특정 리소스의 데이터를 반환
    public GameResourceResponse getResource(Long gameId, Long resourceId) {
        return gameResourceService.getResource(gameId, resourceId);
    }

    // 등록된 리소스 목록을 반환
    public CustomPageImpl<GameResourceResponse> getResources(Long gameId, Long cursorId, Pageable pageable,
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
    public GamePlayResponse createPlayRoom(Long gameId, GamePlayRoundRequest roundRequest, HttpServletRequest request) {
        return gamePlayService.createPlayRoom(gameId, roundRequest, request);
    }

    // 게임 선택 저장 및 다음 선택지 반환
    public GamePlayResponse updatePlayRoom(Long gameId, Long playId, GamePlayRequest request) {
        return gamePlayService.updatePlayRoom(gameId, playId, request);
    }

    // 게임 이어 하기
    public GamePlayResponse continuePlayRoom(Long gameId, Long playId, String inviteCode, HttpServletRequest request) {
        return gamePlayService.continuePlayRoom(gameId, playId, inviteCode, request);
    }

    // 게임의 전반적인 명세 데이터 출력하기
    public GameInfoResponse getGameDetails(Long gameId) {
        return gamePlayService.getGameDetails(gameId);
    }

    // 리소스를 삭제함
    public void deleteResource(Long roomId, Long resourceId, HttpServletRequest request) {
        this.validateRequest(roomId, request);
        gameResourceService.deleteResource(resourceId);
    }

    // 리소스를 선택 삭제함
    public void deleteSelectResources(Long roomId, List<Long> list, HttpServletRequest request) {
        this.validateRequest(roomId, request);
        gameResourceService.deleteSelectResources(list);
    }

    // 게임 결과창 출력
    public CustomPageImpl<GameResultResponse> getResultRanking(Long gameId, Long cursorId,
                                                     GameResourceSearchRequest request,
                                                     Pageable pageable) {
        return gameResultService.getResultRanking(gameId, cursorId, request, pageable);
    }

    // 게임 리소스(컷툰 식) 부모 댓글 리스트 출력
    public CustomPageImpl<GameResourceParentCommentResponse> getParentCommentsByGameResource(Long resourceId, Long cursorId,
                                                                                             Pageable pageable,
                                                                                             GameCommentSearchRequest searchRequest,
                                                                                             HttpServletRequest request) {
        return gameResourceCommentService.getParentCommentsByGameResource(resourceId, cursorId, pageable, searchRequest, request);
    }

    // 게임 리소스(컷툰 식) 대댓글 리스트 출력
    public CustomPageImpl<GameResourceChildrenCommentResponse> getChildrenCommentsByGameResource(Long parentId, Long cursorId,
                                                                                                 Pageable pageable,
                                                                                                 GameCommentSearchRequest searchRequest,
                                                                                                 HttpServletRequest request) {
        return gameResourceCommentService.getChildrenCommentsByGameResource(parentId, cursorId, pageable, searchRequest, request);
    }

    // 게임 리소스(컷툰 식) 댓글 작성
    public void addResourceComment(Long resourceId, GameResourceCommentRequest commentRequest, HttpServletRequest request) {
        gameResourceCommentService.addComment(resourceId, commentRequest, request);
    }

    // 게임 리소스(컷툰 식) 댓글 수정
    public void updateResourceComment(Long commentId, GameResourceCommentUpdateRequest commentRequest, HttpServletRequest request) {
        gameResourceCommentService.updateComment(commentId, commentRequest, request);
    }

    // 게임 리소스(컷툰 식) 댓글 삭제
    public void deleteResourceComment(Long commentId, HttpServletRequest request) {
        gameResourceCommentService.deleteComment(commentId, request);
    }

    // 게임 결과 댓글 리스트 출력
    public CustomPageImpl<GameResultCommentResponse> getCommentsByGameResult(Long gameId, Long cursorId, Pageable pageable,
                                                                             GameCommentSearchRequest searchRequest,
                                                                             HttpServletRequest request) {
        return gameResultCommentService.getCommentsByGameResult(gameId, cursorId, pageable, searchRequest, request);
    }

    // 게임 결과 댓글 작성
    public void addResultComment(Long gameId, GameResultCommentRequest commentRequest, HttpServletRequest request) {
        gameResultCommentService.addComment(gameId, commentRequest, request);
    }

    // 게임 결과 댓글 수정
    public void updateResultComment(Long commentId, GameResultCommentRequest commentRequest, HttpServletRequest request) {
        gameResultCommentService.updateComment(commentId, commentRequest, request);
    }

    // 게임 결과 댓글 삭제
    public void deleteResultComment(Long commentId, HttpServletRequest request) {
        gameResultCommentService.deleteComment(commentId, request);
    }

    // 좋아요 처리 서비스
    public void toggleLike(Long commentId, boolean isLiked, CommentType commentType, HttpServletRequest request) {
        gameCommentLikesService.toggleLike(commentId, isLiked, commentType, request);
    }

    // api 요청한 유저가 해당 게임방 주인이 맞는지 확인.
    private void validateRequest(Long roomId, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);

        if (!gameRepository.existsByIdAndUsers(roomId, users)) {
            throw new UnAuthorizedException("정보가 일치하지 않습니다.", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }
    }
}
