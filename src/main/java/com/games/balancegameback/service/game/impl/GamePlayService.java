package com.games.balancegameback.service.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.BadRequestException;
import com.games.balancegameback.core.exception.impl.UnAuthorizedException;
import com.games.balancegameback.domain.game.GamePlay;
import com.games.balancegameback.domain.game.GameResources;
import com.games.balancegameback.domain.game.GameResults;
import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.game.enums.AccessType;
import com.games.balancegameback.dto.game.gameplay.*;
import com.games.balancegameback.service.game.repository.GamePlayRepository;
import com.games.balancegameback.service.game.repository.GameRepository;
import com.games.balancegameback.service.game.repository.GameResourceRepository;
import com.games.balancegameback.service.game.repository.GameResultRepository;
import com.games.balancegameback.service.user.impl.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class GamePlayService {

    private final GameRepository gameRepository;
    private final GameResourceRepository gameResourceRepository;
    private final GamePlayRepository gamePlayRepository;
    private final GameResultRepository gameResultRepository;
    private final UserUtils userUtils;

    /**
     * 랜덤 게임 id 출력
     */
    public Long getRandomGamePlayId() {
        return gamePlayRepository.findRandomGamePlayId();
    }

    /**
     * 게임의 전반적인 명세 데이터 출력하기
     */
    public GameInfoResponse getGameDetails(Long gameId) {
        Games games = gameRepository.findByRoomId(gameId);
        int totalNums = gameResourceRepository.countByGameId(gameId);

        return GameInfoResponse.builder()
                .title(games.getTitle())
                .description(games.getDescription())
                .totalResourceNums(totalNums)
                .accessType(games.getAccessType())
                .build();
    }

    /**
     * 게임 이어 하기
     */
    public GamePlayResponse continuePlayRoom(Long gameId, Long playId, String inviteCode, HttpServletRequest request) {
        Games games = gameRepository.findByRoomId(gameId);

        if (games.getAccessType().equals(AccessType.PROTECTED)) {
            if (!Objects.equals(games.getGameInviteCode().getInviteCode(), inviteCode)) {
                throw new UnAuthorizedException("일치하지 않는 초대 코드입니다.", ErrorCode.NOT_ALLOW_NO_ACCESS);
            }
        }

        if (games.getAccessType().equals(AccessType.PRIVATE)) {
            String email = userUtils.getEmail(request);
            if (!Objects.equals(games.getUsers().getEmail(), email)) {
                throw new BadRequestException("접근 권한이 없습니다.", ErrorCode.RUNTIME_EXCEPTION);
            }
        }

        GamePlay gamePlay = gamePlayRepository.findById(playId);

        if (gamePlay.isGameEnded()) {
            throw new BadRequestException("이미 종료된 게임입니다.", ErrorCode.CLOSED_PLAYROOM_EXCEPTION);
        }

        List<Long> resourceList = gamePlay.getAllResources();
        List<Long> selectedResourceIds = this.shuffle(resourceList);

        List<GamePlayResourceResponse> selectedResources = gameResourceRepository.findByIds(selectedResourceIds);

        GamePlayResourceResponse leftResource = selectedResources.get(0);
        GamePlayResourceResponse rightResource = selectedResources.get(1);

        return GamePlayResponse.builder()
                .totalRoundNums(gamePlay.getRoundNumber())
                .currentRoundNums(gamePlay.getSelectedResources().size() + 1)
                .playId(gamePlay.getId())
                .leftResource(leftResource)
                .rightResource(rightResource)
                .build();
    }

    /**
     * 게임방 생성 및 게임 시작
     */
    @Transactional
    public GamePlayResponse createPlayRoom(Long gameId, GamePlayRoundRequest roundRequest, HttpServletRequest request) {

        if (!gameRepository.existsGameRounds(gameId, roundRequest.getRoundNumber())) {
            throw new BadRequestException("리소스 수보다 많은 라운드는 실행할 수 없습니다.", ErrorCode.INVALID_ROUND_EXCEPTION);
        }

        Games games = gameRepository.findByRoomId(gameId);

        if (games.getAccessType().equals(AccessType.PROTECTED)) {
            if (!Objects.equals(games.getGameInviteCode().getInviteCode(), roundRequest.getInviteCode())) {
                throw new UnAuthorizedException("일치하지 않는 초대 코드입니다.", ErrorCode.NOT_ALLOW_NO_ACCESS);
            }
        }

        if (games.getAccessType().equals(AccessType.PRIVATE)) {
            String email = userUtils.getEmail(request);
            if (!Objects.equals(games.getUsers().getEmail(), email)) {
                throw new BadRequestException("접근 권한이 없습니다.", ErrorCode.RUNTIME_EXCEPTION);
            }
        }

        List<Long> resourceList = gameResourceRepository.findByRandomId(gameId, roundRequest.getRoundNumber());
        List<Long> selectedResourceIds = this.shuffle(resourceList);

        GamePlay gamePlay = GamePlay.builder()
                .games(games)
                .roundNumber(roundRequest.getRoundNumber())
                .allResources(resourceList)
                .selectedResources(new ArrayList<>())
                .gameEnded(false)
                .build();

        gamePlay = gamePlayRepository.save(gamePlay);

        List<GamePlayResourceResponse> selectedResources = gameResourceRepository.findByIds(selectedResourceIds);

        GamePlayResourceResponse leftResource = selectedResources.get(0);
        GamePlayResourceResponse rightResource = selectedResources.get(1);

        return GamePlayResponse.builder()
                .totalRoundNums(roundRequest.getRoundNumber())
                .currentRoundNums(1)
                .playId(gamePlay.getId())
                .leftResource(leftResource)
                .rightResource(rightResource)
                .build();
    }

    /**
     * 사용자 선택 저장 및 상태 업데이트 & 다음 리소스 페어 반환
     */
    @Transactional
    public GamePlayResponse updatePlayRoom(Long gameId, Long playRoomId, GamePlayRequest gamePlayRequest) {
        GamePlay gamePlay = gamePlayRepository.findById(playRoomId);

        if (gamePlay.isGameEnded()) {
            throw new BadRequestException("이미 종료된 게임입니다.", ErrorCode.CLOSED_PLAYROOM_EXCEPTION);
        }

        gamePlay.updateSelectedResource(gamePlayRequest);

        if (gamePlay.getAllResources().isEmpty()) {
            this.moveToNextRound(gamePlay);

            // 모든 라운드가 끝난 경우 게임 종료
            if (gamePlay.getRoundNumber() <= 1) {
                this.endGame(gamePlay, gamePlayRequest.getWinResourceId());

                return GamePlayResponse.builder()
                        .playId(gamePlay.getId())
                        .totalRoundNums(gamePlay.getRoundNumber())
                        .currentRoundNums(1)
                        .leftResource(null)
                        .rightResource(null)
                        .build();
            }
        }

        List<Long> resourceList = gamePlay.getAllResources();
        List<Long> selectedResourceIds = this.shuffle(resourceList);

        gamePlayRepository.update(gamePlay);

        List<GamePlayResourceResponse> selectedResources = gameResourceRepository.findByIds(selectedResourceIds);

        GamePlayResourceResponse leftResource = selectedResources.get(0);
        GamePlayResourceResponse rightResource = selectedResources.get(1);

        return GamePlayResponse.builder()
                .playId(playRoomId)
                .totalRoundNums(gamePlay.getRoundNumber())
                .currentRoundNums(gamePlay.getSelectedResources().size() + 1)
                .leftResource(leftResource)
                .rightResource(rightResource)
                .build();
    }

    /**
     * 게임 종료 처리
     */
    private void endGame(GamePlay gamePlay, Long resourceId) {
        gamePlay.setGameEnded(true);
        gamePlayRepository.update(gamePlay);

        GameResources gameResources = gameResourceRepository.findById(resourceId);
        GameResults results = GameResults.builder()
                .gameResources(gameResources)
                .build();

        gameResultRepository.save(results);
    }

    /**
     * 다음 게임에 필요한 리소스 페어를 셔플함.
     */
    private List<Long> shuffle(List<Long> resourceList) {
        List<Long> selectedResourceIds = new ArrayList<>(resourceList);
        Collections.shuffle(selectedResourceIds);
        return selectedResourceIds.subList(0, 2);
    }

    private void moveToNextRound(GamePlay gamePlay) {
        // 현재 라운드가 끝났을 때 다음 라운드로 전환
        List<Long> selectedResources = gamePlay.getSelectedResources();

        // 다음 라운드 리소스를 설정
        gamePlay.setAllResources(new ArrayList<>(selectedResources));
        gamePlay.setSelectedResources(new ArrayList<>()); // 선택된 리소스 초기화

        // 라운드 숫자 감소 (ex. 64 -> 32 -> 16)
        gamePlay.setRoundNumber(gamePlay.getRoundNumber() / 2);
    }
}
