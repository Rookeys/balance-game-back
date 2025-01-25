package com.games.balancegameback.service.game.impl;

import com.games.balancegameback.domain.game.GamePlay;
import com.games.balancegameback.domain.game.GameResults;
import com.games.balancegameback.service.game.repository.GamePlayRepository;
import com.games.balancegameback.service.game.repository.GameResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GamePlayService {

    private final GamePlayRepository gamePlayRepository;
    private final GameResultRepository gameResultRepository;

    /**
     * 다음 리소스 페어를 반환
     */
    public List<Long> getNextPair(Long gamePlayId) {
        GamePlay gamePlay = findGamePlayById(gamePlayId);
        return gamePlay.getNextPair(); // 다음 2개의 리소스 반환
    }

    /**
     * 사용자 선택 저장 및 상태 업데이트
     */
    public void selectResource(Long gamePlayId, Long selectedResourceId) {
        GamePlay gamePlay = findGamePlayById(gamePlayId);

        // 선택된 리소스를 업데이트
        gamePlay.updateSelectedResource(selectedResourceId);

        // 업데이트 후 저장
        gamePlayRepository.save(gamePlay);

        // 모든 리소스를 선택한 경우 게임 종료 처리
        if (gamePlay.getAllResources().isEmpty()) {
            endGame(gamePlay);
        }
    }

    /**
     * 게임 종료 처리
     */
    private void endGame(GamePlay gamePlay) {
        GameResults gameResults = GameResults.builder().build();
        gameResultRepository.save(gameResults);
        gamePlayRepository.delete(gamePlay);
    }

    /**
     * GamePlay 조회
     */
    private GamePlay findGamePlayById(Long gamePlayId) {
        return gamePlayRepository.findById(gamePlayId);
    }
}
