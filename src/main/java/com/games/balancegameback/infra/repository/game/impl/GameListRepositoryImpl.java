package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.enums.Category;
import com.games.balancegameback.domain.game.enums.GameListType;
import com.games.balancegameback.domain.game.enums.GameSortType;
import org.springframework.transaction.annotation.Transactional;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.*;
import com.games.balancegameback.infra.repository.game.common.CommonGameRepository;
import com.games.balancegameback.infra.repository.game.common.GameBatchData;
import com.games.balancegameback.infra.repository.game.common.GameConstants;
import com.games.balancegameback.infra.repository.game.common.GamePlayCounts;
import com.games.balancegameback.infra.repository.game.common.GameQClasses;
import com.games.balancegameback.infra.repository.game.service.GameQueryService;
import com.games.balancegameback.infra.repository.game.strategy.GameListStrategy;
import com.games.balancegameback.infra.repository.game.strategy.GameListStrategyFactory;
import com.games.balancegameback.service.game.repository.GameListRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 게임 리스트 Repository 구현체
 * 전략 패턴 + 컴포지션을 활용한 조율자 역할
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class GameListRepositoryImpl implements GameListRepository {
    
    private final GameListStrategyFactory strategyFactory;
    private final GameQueryService gameQueryService;
    private final CommonGameRepository commonGameRepository;
    private final JPAQueryFactory jpaQueryFactory;
    
    @Override
    @Transactional(readOnly = true)
    public CustomPageImpl<GameListResponse> getGameList(Long cursorId, Pageable pageable,
                                                        GameSearchRequest searchRequest, Users users) {
        try {
            long t0 = System.currentTimeMillis();
            log.info("[PERF] getGameList start - cursorId: {}, pageSize: {}", cursorId, pageable.getPageSize());

            GameListStrategy strategy = strategyFactory.getStrategy(GameListType.PUBLIC);
            BooleanBuilder conditions = strategy.buildFilterConditions(searchRequest, users);
            GameSortType sortType = searchRequest.getSortType();

            // 전체 게임 ID 경량 조회
            List<Long> allGameIds = gameQueryService.fetchAllGameIds(conditions, strategy.requiresResourceCountValidation());
            long t1 = System.currentTimeMillis();
            log.info("[PERF] fetchAllGameIds: {}ms, count={}", t1 - t0, allGameIds.size());

            if (allGameIds.isEmpty()) {
                return new CustomPageImpl<>(Collections.emptyList(), pageable, 0L, cursorId, false);
            }

            // 전체 플레이 카운트 조회
            Map<Long, GamePlayCounts> allPlayCounts = commonGameRepository.getPlayCountsBatch(allGameIds, sortType);
            long t2 = System.currentTimeMillis();
            log.info("[PERF] getPlayCountsBatch (all, sort): {}ms", t2 - t1);

            // Phase 2: 메모리 정렬 후 커서 페이징 → 화면에 보일 ID만 추출
            List<Long> sortedIds = sortIds(allGameIds, sortType, allPlayCounts);
            List<Long> pagedIds = applyIdCursorPaging(sortedIds, cursorId, pageable);
            boolean hasNext = pagedIds.size() > pageable.getPageSize();
            if (hasNext) pagedIds = new ArrayList<>(pagedIds.subList(0, pageable.getPageSize()));

            // 페이징된 ID에 대해서만 상세 데이터 조회
            List<Tuple> tuples = gameQueryService.fetchGameBasicDataByIds(pagedIds, users);
            long t3 = System.currentTimeMillis();
            log.info("[PERF] fetchGameBasicDataByIds: {}ms, count={}", t3 - t2, tuples.size());

            var categoriesMap = commonGameRepository.getCategoriesBatch(pagedIds);
            long t4 = System.currentTimeMillis();
            log.info("[PERF] getCategoriesBatch: {}ms", t4 - t3);

            var selectionsMap = commonGameRepository.getTopResourcesBatch(pagedIds);
            long t5 = System.currentTimeMillis();
            log.info("[PERF] getTopResourcesBatch: {}ms", t5 - t4);

            // 정렬용으로 이미 가져온 플레이 카운트 재사용
            Map<Long, GamePlayCounts> pagedPlayCounts = pagedIds.stream()
                    .collect(Collectors.toMap(id -> id,
                            id -> allPlayCounts.getOrDefault(id, new GamePlayCounts(0, 0, 0))));
            GameBatchData batchData = GameBatchData.of(categoriesMap, selectionsMap, pagedPlayCounts);

            // 응답 생성 후 pagedIds 순서 유지
            Map<Long, GameListResponse> responseMap = tuples.stream()
                    .map(t -> commonGameRepository.buildGameListResponse(t, users, batchData))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toMap(GameListResponse::getRoomId, r -> r));

            List<GameListResponse> orderedResponses = pagedIds.stream()
                    .map(responseMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            long t6 = System.currentTimeMillis();
            log.info("[PERF] build responses: {}ms", t6 - t5);
            log.info("[PERF] getGameList TOTAL: {}ms", t6 - t0);

            // totalElements = allGameIds.size() — 별도 COUNT 쿼리 불필요
            return new CustomPageImpl<>(orderedResponses, pageable, (long) allGameIds.size(), cursorId, hasNext);
        } catch (Exception e) {
            log.error("Error in getGameList", e);
            return new CustomPageImpl<>(Collections.emptyList(), pageable, 0L, cursorId, false);
        }
    }

    private static final GamePlayCounts EMPTY_COUNTS = new GamePlayCounts(0, 0, 0);

    private List<Long> sortIds(List<Long> ids, GameSortType sortType, Map<Long, GamePlayCounts> counts) {
        Comparator<Long> comparator = switch (sortType) {
            case RECENT    -> Comparator.reverseOrder();
            case OLD       -> Comparator.naturalOrder();
            case WEEK      -> Comparator.comparingInt((Long id) -> counts.getOrDefault(id, EMPTY_COUNTS).weekPlays())
                                        .reversed().thenComparing(Comparator.reverseOrder());
            case MONTH     -> Comparator.comparingInt((Long id) -> counts.getOrDefault(id, EMPTY_COUNTS).monthPlays())
                                        .reversed().thenComparing(Comparator.reverseOrder());
            case PLAY_DESC -> Comparator.comparingInt((Long id) -> counts.getOrDefault(id, EMPTY_COUNTS).totalPlays())
                                        .reversed().thenComparing(Comparator.reverseOrder());
        };
        return ids.stream().sorted(comparator).collect(Collectors.toList());
    }

    private List<Long> applyIdCursorPaging(List<Long> sortedIds, Long cursorId, Pageable pageable) {
        int start = 0;
        if (cursorId != null) {
            int idx = sortedIds.indexOf(cursorId);
            if (idx == -1) return Collections.emptyList();
            start = idx + 1;
        }
        return sortedIds.stream()
                .skip(start)
                .limit(pageable.getPageSize() + 1L)
                .collect(Collectors.toList());
    }
    
    @Override
    @Cacheable(value = "game-category-counts", key = "#title != null && !#title.isEmpty() ? #title : 'all'")
    public GameCategoryNumsResponse getCategoryCounts(String title) {
        try {
            log.info("Getting category counts - title: {}", title);
            
            // PUBLIC 전략 사용
            GameListStrategy strategy = strategyFactory.getStrategy(GameListType.PUBLIC);
            
            GameSearchRequest request = GameSearchRequest.builder()
                    .title(title)
                    .build();
            
            BooleanBuilder conditions = strategy.buildFilterConditions(request, null);
            
            // 카테고리별 개수 조회
            Map<Category, Long> counts = fetchCategoryCounts(conditions);
            int total = counts.values().stream()
                    .filter(Objects::nonNull)
                    .mapToInt(Long::intValue)
                    .sum();
            
            return GameCategoryNumsResponse.builder()
                    .totalNums(total)
                    .categoryNums(counts)
                    .build();
        } catch (Exception e) {
            log.error("Error fetching category counts", e);
            return createEmptyCategoryResponse();
        }
    }
    
    @Override
    public GameDetailResponse getGameStatus(Long gameId, Users user) {
        try {
            // 접근 권한 확인
            if (!commonGameRepository.isGameAccessibleByUser(gameId, user)) {
                log.warn("Game not accessible - gameId: {}, user: {}", gameId, user != null ? user.getUid() : "null");
                throw new RuntimeException("접근 불가");
            }
            
            // 기본 데이터 조회
            Tuple gameData = gameQueryService.fetchGameDetailData(gameId, user);
            if (gameData == null) {
                log.warn("Game not found - gameId: {}", gameId);
                throw new RuntimeException("Game not found");
            }
            
            // 배치 데이터 조회
            GameBatchData batchData = commonGameRepository.getTotalPlayBatchData(List.of(gameId));
            List<Category> categories = batchData.getCategoriesMap().getOrDefault(gameId, Collections.emptyList());
            List<GameListSelectionResponse> selections = batchData.getSelectionsMap().getOrDefault(gameId, Collections.emptyList());

            return gameQueryService.buildGameDetailResponse(gameData, categories, selections);
        } catch (Exception e) {
            log.error("Error fetching game detail for gameId: {}", gameId, e);
            throw new RuntimeException("Game detail fetch failed", e);
        }
    }
    
    /**
     * 카테고리별 게임 개수 조회
     * conditions에 따라 필터링된 게임들의 카테고리별 개수를 반환
     * 
     * @param conditions 필터 조건
     * @return 카테고리별 게임 개수 맵
     */
    private Map<Category, Long> fetchCategoryCounts(BooleanBuilder conditions) {
        // 모든 카테고리를 0으로 초기화
        Map<Category, Long> counts = new EnumMap<>(Category.class);
        Arrays.stream(Category.values()).forEach(cat -> counts.put(cat, 0L));
        
        // 실제 카테고리별 게임 개수 조회
        List<Tuple> result = jpaQueryFactory
                .select(
                    GameQClasses.category.category,
                    GameQClasses.games.id.countDistinct()
                )
                .from(GameQClasses.games)
                .join(GameQClasses.games.categories, GameQClasses.category)
                .join(GameQClasses.games.users, GameQClasses.users)
                .leftJoin(GameQClasses.games.gameResources, GameQClasses.resources)
                .where(conditions)  // 전략에서 생성된 조건 적용 (제목 검색 등)
                .groupBy(GameQClasses.category.category)
                .having(GameQClasses.games.gameResources.size().goe(GameConstants.MIN_RESOURCE_COUNT))
                .fetch();

        result.forEach(tuple -> {
            Category cat = tuple.get(GameQClasses.category.category);
            Long count = Optional.ofNullable(tuple.get(GameQClasses.games.id.countDistinct()))
                    .orElse(0L);
            if (cat != null) {
                counts.put(cat, count);
            }
        });

        return counts;
    }
    
    /**
     * 빈 카테고리 응답 생성
     */
    private GameCategoryNumsResponse createEmptyCategoryResponse() {
        Map<Category, Long> emptyCounts = new EnumMap<>(Category.class);
        Arrays.stream(Category.values()).forEach(cat -> emptyCounts.put(cat, 0L));
        
        return GameCategoryNumsResponse.builder()
                .totalNums(0)
                .categoryNums(emptyCounts)
                .build();
    }
}
