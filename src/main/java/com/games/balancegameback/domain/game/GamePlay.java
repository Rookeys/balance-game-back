package com.games.balancegameback.domain.game;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.infra.entity.GamesEntity;
import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
public class GamePlay {

    private Long id;
    private GamesEntity games;
    private int roundNumber;
    private List<Long> allResources;
    private List<Long> selectedResources;

    @Builder
    public GamePlay(Long id, GamesEntity games, int roundNumber, List<Long> allResources, List<Long> selectedResources) {
        this.id = id;
        this.games = games;
        this.roundNumber = roundNumber;
        this.allResources = new ArrayList<>(allResources);
        this.selectedResources = new ArrayList<>(selectedResources);
    }

    /**
     * 선택된 리소스를 업데이트하고 전체 리소스에서 제거
     */
    public void updateSelectedResource(Long resourceId) {
        if (!allResources.contains(resourceId)) {
            throw new NotFoundException("Resource ID not found", ErrorCode.NOT_FOUND_EXCEPTION);
        }
        if (selectedResources.contains(resourceId)) {
            throw new NotFoundException("Resource ID not found", ErrorCode.NOT_FOUND_EXCEPTION);
        }
        allResources.remove(resourceId);
        selectedResources.add(resourceId);
    }

    /**
     * 현재 전체 리소스에서 랜덤한 다음 두 개의 리소스를 반환
     */
    public List<Long> getNextPair() {
        if (allResources.size() < 2) {
            throw new IllegalStateException("Not enough resources to create a pair.");
        }

        // 랜덤으로 리스트 복사본 섞기
        List<Long> shuffledResources = new ArrayList<>(allResources);
        Collections.shuffle(shuffledResources);

        return shuffledResources.subList(0, 2);
    }

    /**
     * 방어적 복사로 리스트 반환
     */
    public List<Long> getAllResources() {
        return Collections.unmodifiableList(allResources);
    }

    public List<Long> getSelectedResources() {
        return Collections.unmodifiableList(selectedResources);
    }
}
