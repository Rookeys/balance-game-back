package com.games.balancegameback.infra.repository.game.common;

import com.games.balancegameback.domain.game.enums.Category;
import com.games.balancegameback.domain.game.enums.GameSortType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.GameListResponse;
import com.games.balancegameback.dto.game.GameListSelectionResponse;
import com.querydsl.core.Tuple;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * 게임 관련 공통 Repository 인터페이스
 */
public interface CommonGameRepository {

    // =========================== 배치 조회 메서드들 ===========================

    /**
     * 게임 ID 목록으로 카테고리 배치 조회
     */
    Map<Long, List<Category>> getCategoriesBatch(List<Long> gameIds);

    /**
     * 게임 ID 목록으로 상위 리소스 배치 조회
     */
    Map<Long, List<GameListSelectionResponse>> getTopResourcesBatch(List<Long> gameIds);

    /**
     * 게임 ID 목록으로 플레이 카운트 배치 조회 (정렬 타입에 따라 필요한 것만)
     */
    Map<Long, GamePlayCounts> getPlayCountsBatch(List<Long> gameIds, GameSortType sortType);

    /**
     * 게임 ID 목록으로 총 플레이 카운트만 배치 조회
     */
    Map<Long, Integer> getTotalPlayCountsBatch(List<Long> gameIds);

    /**
     * 모든 배치 데이터를 한 번에 조회
     */
    GameBatchData getAllBatchData(List<Long> gameIds, GameSortType sortType);

    /**
     * 총 플레이 카운트만 있는 배치 데이터 조회
     */
    GameBatchData getTotalPlayBatchData(List<Long> gameIds);

    // =========================== 페이징 메서드들 ===========================

    /**
     * 커스텀 커서 추출 함수를 사용한 커서 페이징
     */
    <T> List<T> applyCursorPagingWithCustomCursor(List<T> sortedResponses, Long cursorId,
                                                  Function<T, Long> cursorExtractor, Pageable pageable);

    <T extends CursorIdentifiable> List<T> applyCursorPaging(List<T> sortedResponses, Long cursorId, Pageable pageable);

    // =========================== 응답 생성 메서드들 ===========================

    /**
     * Tuple로부터 GameListSelectionResponse 생성
     */
    GameListSelectionResponse buildSelectionResponse(Tuple tuple);

    /**
     * Tuple과 배치 데이터로부터 GameListResponse 생성
     */
    GameListResponse buildGameListResponse(Tuple tuple, Users currentUser, GameBatchData batchData);

    // =========================== 유틸리티 메서드들 ===========================

    /**
     * Long 값을 안전하게 int로 변환
     */
    int safeIntValue(Long value);

    /**
     * 사용자가 게임에 접근 가능한지 확인
     */
    boolean isGameAccessibleByUser(Long gameId, Users user);

    /**
     * Tuple 목록에서 게임 ID 목록 추출
     */
    List<Long> extractGameIds(List<Tuple> tuples);
}
