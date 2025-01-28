package com.games.balancegameback.service.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.BadRequestException;
import com.games.balancegameback.domain.game.GamePlay;
import com.games.balancegameback.domain.game.GameResources;
import com.games.balancegameback.domain.game.GameResults;
import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.dto.game.gameplay.GamePlayRequest;
import com.games.balancegameback.dto.game.gameplay.GamePlayResourceResponse;
import com.games.balancegameback.dto.game.gameplay.GamePlayResponse;
import com.games.balancegameback.dto.game.gameplay.GamePlayRoundRequest;
import com.games.balancegameback.service.game.repository.GamePlayRepository;
import com.games.balancegameback.service.game.repository.GameRepository;
import com.games.balancegameback.service.game.repository.GameResourceRepository;
import com.games.balancegameback.service.game.repository.GameResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GamePlayService {

    private final GameRepository gameRepository;
    private final GameResourceRepository gameResourceRepository;
    private final GamePlayRepository gamePlayRepository;
    private final GameResultRepository gameResultRepository;

    /**
     * 게임방 생성 및 게임 시작
     */
    @Transactional
    public GamePlayResponse createPlayRoom(Long gameId, GamePlayRoundRequest request) {
        Games games = gameRepository.findByRoomId(gameId);

        List<Long> resourceList = gameResourceRepository.findByRandomId(gameId, request.getRoundNumber());
        List<Long> selectedResourceIds = this.shuffle(resourceList);

        for (Long resourceId : selectedResourceIds) {
            resourceList.remove(resourceId);
        }

        GamePlay gamePlay = GamePlay.builder()
                .games(games)
                .roundNumber(request.getRoundNumber())
                .allResources(resourceList)
                .selectedResources(new ArrayList<>())
                .gameEnded(false)
                .build();

        gamePlayRepository.save(gamePlay);

        List<GamePlayResourceResponse> selectedResources = gameResourceRepository.findByIds(selectedResourceIds);

        GamePlayResourceResponse leftResource = selectedResources.get(0);
        GamePlayResourceResponse rightResource = selectedResources.get(1);

        return GamePlayResponse.builder()
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

        if (gamePlay.getAllResources().isEmpty()) {
            throw new BadRequestException("이미 끝난 게임입니다.", ErrorCode.CLOSED_PLAYROOM_EXCEPTION);
        }

        gamePlay.updateSelectedResource(gamePlayRequest);

        if (gamePlay.getAllResources().isEmpty()) {
            this.moveToNextRound(gamePlay);

            // 모든 라운드가 끝난 경우 게임 종료
            if (gamePlay.getRoundNumber() == 1) {
                this.endGame(gamePlay);
                return null;
            }
        }

        List<Long> resourceList = gamePlay.getAllResources();
        List<Long> selectedResourceIds = this.shuffle(resourceList);

        gamePlayRepository.update(gamePlay);

        List<GamePlayResourceResponse> selectedResources = gameResourceRepository.findByIds(selectedResourceIds);

        GamePlayResourceResponse leftResource = selectedResources.get(0);
        GamePlayResourceResponse rightResource = selectedResources.get(1);

        return GamePlayResponse.builder()
                .leftResource(leftResource)
                .rightResource(rightResource)
                .build();
    }

    /**
     * 게임 종료 처리
     */
    private void endGame(GamePlay gamePlay) {
        gamePlay.setGameEnded(true);
        gamePlayRepository.update(gamePlay);
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

        // 라운드 숫자 감소 (예: 64 -> 32 -> 16)
        gamePlay.setRoundNumber(gamePlay.getRoundNumber() / 2);
    }
}
