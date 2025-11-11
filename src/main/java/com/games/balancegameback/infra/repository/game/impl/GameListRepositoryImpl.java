package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.enums.Category;
import com.games.balancegameback.domain.game.enums.GameListType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.*;
import com.games.balancegameback.infra.repository.game.common.CommonGameRepository;
import com.games.balancegameback.infra.repository.game.common.GameBatchData;
import com.games.balancegameback.infra.repository.game.common.GameConstants;
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
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.*;

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
    public CustomPageImpl<GameListResponse> getGameList(Long cursorId, Pageable pageable,
                                                        GameSearchRequest searchRequest, Users users) {
        try {
            log.info("Getting game list - cursorId: {}, pageSize: {}, request: {}", 
                     cursorId, pageable.getPageSize(), searchRequest);
            
            // 전략 선택
            GameListStrategy strategy = strategyFactory.getStrategy(GameListType.PUBLIC);
            
            // 필터 조건 생성
            BooleanBuilder conditions = strategy.buildFilterConditions(searchRequest, users);

            List<Tuple> baseTuples = gameQueryService.fetchGameBasicData(
                conditions,
                users,
                strategy.requiresResourceCountValidation()
            );
            
            if (baseTuples.isEmpty()) {
                log.info("No games found for the given criteria");
                return new CustomPageImpl<>(Collections.emptyList(), pageable, 0L, cursorId, false);
            }
            
            // 응답 생성
            List<GameListResponse> responses = gameQueryService.buildGameListResponses(
                baseTuples,
                users,
                searchRequest.getSortType()
            );
            
            // 정렬
            List<GameListResponse> sortedResponses = gameQueryService.applySorting(
                responses,
                searchRequest.getSortType()
            );
            
            // 페이징
            List<GameListResponse> pagedResponses = commonGameRepository.applyCursorPagingWithCustomCursor(
                sortedResponses,
                cursorId,
                GameListResponse::getRoomId,
                pageable
            );
            
            boolean hasNext = pagedResponses.size() > pageable.getPageSize();
            if (hasNext) {
                pagedResponses.removeLast();
            }
            
            // 총 개수 계산
            Long totalElements = gameQueryService.calculateTotalElements(
                conditions,
                strategy.requiresResourceCountValidation()
            );
            
            return new CustomPageImpl<>(pagedResponses, pageable, totalElements, cursorId, hasNext);
        } catch (Exception e) {
            log.error("Error in getGameList", e);
            return new CustomPageImpl<>(Collections.emptyList(), pageable, 0L, cursorId, false);
        }
    }
    
    @Override
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

            return gameQueryService.buildGameDetailResponse(gameData, categories, selections, user);
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
