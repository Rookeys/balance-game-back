package com.games.balancegameback.domain.game;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.BadRequestException;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.dto.game.gameplay.GamePlayRequest;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class GamePlay {

    private Long id;
    private Games games;
    private int roundNumber;
    private List<Long> allResources;
    private List<Long> selectedResources;
    private boolean gameEnded;

    @Builder
    public GamePlay(Long id, Games games, int roundNumber, List<Long> allResources,
                    List<Long> selectedResources, boolean gameEnded) {
        this.id = id;
        this.games = games;
        this.roundNumber = roundNumber;
        this.allResources = new ArrayList<>(allResources);
        this.selectedResources = new ArrayList<>(selectedResources);
        this.gameEnded = gameEnded;
    }

    /**
     * 선택된 리소스를 업데이트
     */
    public void updateSelectedResource(GamePlayRequest gamePlayRequest) {
        if (!allResources.contains(gamePlayRequest.getWinResourceId()) && !allResources.contains(gamePlayRequest.getLoseResourceId())) {
            throw new NotFoundException("Resource ID not found", ErrorCode.NOT_FOUND_EXCEPTION);
        }

        if (selectedResources.contains(gamePlayRequest.getWinResourceId()) || selectedResources.contains(gamePlayRequest.getLoseResourceId())) {
            throw new BadRequestException("Resource ID not found", ErrorCode.RUNTIME_EXCEPTION);
        }

        this.getAllResources().remove(gamePlayRequest.getWinResourceId());
        this.getAllResources().remove(gamePlayRequest.getLoseResourceId());

        this.selectedResources.add(gamePlayRequest.getWinResourceId());
    }

    public List<Long> getAllResources() {
        return Collections.unmodifiableList(allResources);
    }

    public List<Long> getSelectedResources() {
        return Collections.unmodifiableList(selectedResources);
    }
}
