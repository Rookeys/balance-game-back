package com.games.balancegameback.infra.repository.game.common;

import com.games.balancegameback.domain.game.enums.Category;
import com.games.balancegameback.dto.game.GameListSelectionResponse;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

/**
 * 배치 조회 결과 데이터
 */
@Builder
@Getter
public class GameBatchData {

    private final Map<Long, List<Category>> categoriesMap;
    private final Map<Long, List<GameListSelectionResponse>> selectionsMap;
    private final Map<Long, GamePlayCounts> playCountsMap;
    private final Map<Long, Integer> totalPlayCountsMap;

    /**
     * 간편한 GameBatchData 생성을 위한 정적 메서드
     */
    public static GameBatchData of(Map<Long, List<Category>> categoriesMap,
                                   Map<Long, List<GameListSelectionResponse>> selectionsMap,
                                   Map<Long, GamePlayCounts> playCountsMap) {
        return GameBatchData.builder()
                .categoriesMap(categoriesMap)
                .selectionsMap(selectionsMap)
                .playCountsMap(playCountsMap)
                .build();
    }

    /**
     * 총 플레이 카운트만 있는 경우를 위한 정적 메서드
     */
    public static GameBatchData ofTotalPlays(Map<Long, List<Category>> categoriesMap,
                                             Map<Long, List<GameListSelectionResponse>> selectionsMap,
                                             Map<Long, Integer> totalPlayCountsMap) {
        return GameBatchData.builder()
                .categoriesMap(categoriesMap)
                .selectionsMap(selectionsMap)
                .totalPlayCountsMap(totalPlayCountsMap)
                .build();
    }
}
