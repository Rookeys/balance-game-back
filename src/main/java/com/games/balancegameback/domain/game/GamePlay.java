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

    private String id;
    private Games games;
    private int roundNumber;
    private List<String> allResources;
    private List<String> selectedResources;
    private boolean gameEnded;

    @Builder
    public GamePlay(String id, Games games, int roundNumber, List<String> allResources,
                    List<String> selectedResources, boolean gameEnded) {
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
        String winResourceId = String.valueOf(gamePlayRequest.getWinResourceId());
        String loseResourceId = String.valueOf(gamePlayRequest.getLoseResourceId());

        if (!allResources.contains(winResourceId) && !allResources.contains(loseResourceId)) {
            throw new NotFoundException("Resource ID not found", ErrorCode.NOT_FOUND_EXCEPTION);
        }

        if (selectedResources.contains(winResourceId) || selectedResources.contains(loseResourceId)) {
            throw new BadRequestException("Resource ID not found", ErrorCode.RUNTIME_EXCEPTION);
        }

        this.allResources.remove(winResourceId);
        this.allResources.remove(loseResourceId);

        this.selectedResources.add(winResourceId);
    }

    public List<String> getAllResources() {
        return Collections.unmodifiableList(allResources);
    }

    public List<String> getSelectedResources() {
        return Collections.unmodifiableList(selectedResources);
    }
}