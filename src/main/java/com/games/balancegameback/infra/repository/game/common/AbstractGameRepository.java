package com.games.balancegameback.infra.repository.game.common;

import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.enums.AccessType;
import com.games.balancegameback.domain.game.enums.Category;
import com.games.balancegameback.domain.game.enums.GameListType;
import com.games.balancegameback.domain.game.enums.GameSortType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.*;
import com.games.balancegameback.dto.user.UserMainResponse;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractGameRepository {

    protected final JPAQueryFactory jpaQueryFactory;
    protected final CommonGameRepository commonGameRepository;

    /**
     * 게임 리스트 조회
     */
    protected CustomPageImpl<GameListResponse> findGameListAutomated(
            Long cursorId, Pageable pageable, GameSearchRequest searchRequest, Users user, GameListType listType) {

        try {
            Long totalElements = calculateTotalElements(searchRequest, user, listType);
            List<Tuple> baseTuples = fetchBaseTuples(searchRequest, user, listType);

            if (baseTuples.isEmpty()) {
                return new CustomPageImpl<>(Collections.emptyList(), pageable, totalElements, cursorId, false);
            }

            List<GameListResponse> responses = buildCompleteGameListResponses(baseTuples, user, searchRequest.getSortType());
            List<GameListResponse> sortedResponses = applySorting(responses, searchRequest.getSortType());
            List<GameListResponse> pagedResponses = applyPaging(sortedResponses, cursorId, pageable);

            boolean hasNext = pagedResponses.size() > pageable.getPageSize();
            if (hasNext) {
                pagedResponses.removeLast();
            }

            return new CustomPageImpl<>(pagedResponses, pageable, totalElements, cursorId, hasNext);
        } catch (Exception e) {
            log.error("Error in findGameListAutomated", e);
            return new CustomPageImpl<>(Collections.emptyList(), pageable, 0L, cursorId, false);
        }
    }

    /**
     * 카테고리 개수 조회
     */
    protected GameCategoryNumsResponse getCategoryCountsAutomated(String title, GameListType listType, Users user) {
        try {
            BooleanBuilder conditions = buildBaseCategoryConditions(title, listType, user);
            Map<Category, Long> counts = fetchCategoryCounts(conditions);
            int total = counts.values().stream().filter(Objects::nonNull).mapToInt(Long::intValue).sum();

            return GameCategoryNumsResponse.builder()
                    .totalNums(total)
                    .categoryNums(counts)
                    .build();
        } catch (Exception e) {
            log.error("Error fetching category counts", e);
            return createEmptyCategoryResponse();
        }
    }

    /**
     * 게임 상세 조회
     */
    protected GameDetailResponse getGameDetailAutomated(Long gameId, Users user) {
        try {
            // 접근 권한 확인
            if (!commonGameRepository.isGameAccessibleByUser(gameId, user)) {
                throw new RuntimeException("접근 불가");
            }

            // 기본 데이터 조회
            Tuple gameData = fetchGameDetailBasicData(gameId, user);
            if (gameData == null) {
                throw new RuntimeException("Game not found");
            }

            // 배치 데이터 조회
            GameBatchData batchData = commonGameRepository.getTotalPlayBatchData(List.of(gameId));
            List<Category> categories = batchData.getCategoriesMap().getOrDefault(gameId, Collections.emptyList());
            List<GameListSelectionResponse> selections = batchData.getSelectionsMap().getOrDefault(gameId, Collections.emptyList());

            return buildGameDetailResponse(gameData, categories, selections, user);
        } catch (Exception e) {
            log.error("Error fetching game detail for gameId: {}", gameId, e);
            throw new RuntimeException("Game detail fetch failed");
        }
    }

    // =========================== 기본 데이터 조회 메서드들 ===========================

    private List<Tuple> fetchBaseTuples(GameSearchRequest searchRequest, Users user, GameListType listType) {
        BooleanBuilder conditions = buildGameListConditions(searchRequest, user, listType);
        Expression<Boolean> existsMineExpr = buildExistsMineExpression(user);

        return jpaQueryFactory
                .select(GameQClasses.games.id,                      // 0
                        GameQClasses.games.title,                   // 1
                        GameQClasses.games.description,             // 2
                        GameQClasses.games.users.nickname,          // 3
                        GameQClasses.images.fileUrl.max(),          // 4
                        GameQClasses.games.isNamePrivate,           // 5
                        GameQClasses.games.createdDate,             // 6
                        GameQClasses.games.isBlind,                 // 7
                        existsMineExpr)                             // 8
                .from(GameQClasses.games)
                .leftJoin(GameQClasses.games.users, GameQClasses.users)
                .leftJoin(GameQClasses.images).on(GameQClasses.images.users.uid.eq(GameQClasses.users.uid))
                .leftJoin(GameQClasses.games.gameResources, GameQClasses.resources)
                .leftJoin(GameQClasses.games.categories, GameQClasses.category)
                .where(conditions)
                .groupBy(GameQClasses.games.id)
                .having(GameQClasses.resources.count().goe(GameConstants.MIN_RESOURCE_COUNT))
                .fetch();
    }

    private Tuple fetchGameDetailBasicData(Long gameId, Users user) {
        Expression<Boolean> existsMineExpr = buildExistsMineExpression(user);

        return jpaQueryFactory
                .select(GameQClasses.games.id, GameQClasses.games.title, GameQClasses.games.description,
                        GameQClasses.games.users.nickname, GameQClasses.games.isNamePrivate,
                        GameQClasses.games.createdDate, GameQClasses.games.updatedDate, GameQClasses.games.isBlind,
                        GameQClasses.images.fileUrl.max(), existsMineExpr,
                        GameQClasses.results.count().coalesce(GameConstants.DEFAULT_COUNT).as("totalPlays"),
                        GameQClasses.resources.count().coalesce(GameConstants.DEFAULT_COUNT).as("totalResources"))
                .from(GameQClasses.games)
                .leftJoin(GameQClasses.games.users, GameQClasses.users)
                .leftJoin(GameQClasses.images).on(GameQClasses.images.users.uid.eq(GameQClasses.users.uid))
                .leftJoin(GameQClasses.games.gameResources, GameQClasses.resources)
                .leftJoin(GameQClasses.results).on(GameQClasses.results.gameResources.eq(GameQClasses.resources))
                .where(GameQClasses.games.id.eq(gameId))
                .groupBy(GameQClasses.games.id)
                .having(GameQClasses.resources.count().goe(GameConstants.MIN_RESOURCE_COUNT))
                .fetchOne();
    }

    // =========================== 조건 빌더 메서드들 ===========================

    private BooleanBuilder buildGameListConditions(GameSearchRequest searchRequest, Users user, GameListType listType) {
        BooleanBuilder builder = new BooleanBuilder();

        // 리스트 타입별 기본 조건
        switch (listType) {
            case PUBLIC -> builder.and(GameQClasses.games.accessType.eq(AccessType.PUBLIC));
            case MY_GAMES -> {
                if (user != null) {
                    builder.and(GameQClasses.games.users.uid.eq(user.getUid()));
                }
            }
            case ALL -> {
                if (user != null) {
                    builder.and(GameQClasses.games.accessType.ne(AccessType.PRIVATE)
                            .or(GameQClasses.games.users.uid.eq(user.getUid())));
                } else {
                    builder.and(GameQClasses.games.accessType.ne(AccessType.PRIVATE));
                }
            }
        }

        // 카테고리 필터
        if (searchRequest.getCategory() != null) {
            builder.and(GameQClasses.category.category.eq(searchRequest.getCategory()));
        }

        // 제목 검색
        if (StringUtils.hasText(searchRequest.getTitle())) {
            BooleanExpression searchCondition = createTitleSearchCondition(searchRequest.getTitle());
            builder.and(searchCondition);
        }

        return builder;
    }

    private BooleanBuilder buildBaseCategoryConditions(String title, GameListType listType, Users user) {
        BooleanBuilder builder = new BooleanBuilder();

        // 리스트 타입별 조건
        switch (listType) {
            case PUBLIC -> builder.and(GameQClasses.games.accessType.eq(AccessType.PUBLIC));
            case MY_GAMES -> {
                if (user != null) {
                    builder.and(GameQClasses.games.users.uid.eq(user.getUid()));
                }
            }
            case ALL -> {
                if (user != null) {
                    builder.and(GameQClasses.games.accessType.ne(AccessType.PRIVATE)
                            .or(GameQClasses.games.users.uid.eq(user.getUid())));
                } else {
                    builder.and(GameQClasses.games.accessType.ne(AccessType.PRIVATE));
                }
            }
        }

        // 제목 검색
        if (StringUtils.hasText(title)) {
            BooleanExpression searchCondition = createTitleSearchCondition(title);
            builder.and(searchCondition);
        }

        return builder;
    }

    private BooleanExpression createTitleSearchCondition(String title) {
        String searchTitle = title.trim();
        return GameQClasses.games.title.containsIgnoreCase(searchTitle)
                .or(GameQClasses.resources.title.containsIgnoreCase(searchTitle))
                .or(GameQClasses.users.nickname.containsIgnoreCase(searchTitle)
                        .and(GameQClasses.games.isNamePrivate.eq(false)));
    }

    private Expression<Boolean> buildExistsMineExpression(Users user) {
        return user != null ?
                GameQClasses.games.users.uid.eq(user.getUid()) :
                Expressions.FALSE;
    }

    // =========================== 응답 생성 메서드들 ===========================

    private List<GameListResponse> buildCompleteGameListResponses(List<Tuple> tuples, Users user, GameSortType sortType) {
        List<Long> gameIds = commonGameRepository.extractGameIds(tuples);
        GameBatchData batchData = commonGameRepository.getAllBatchData(gameIds, sortType);

        return tuples.stream()
                .map(tuple -> commonGameRepository.buildGameListResponse(tuple, user, batchData))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private GameDetailResponse buildGameDetailResponse(Tuple gameData, List<Category> categories,
                                                       List<GameListSelectionResponse> selections, Users user) {
        String nickname = gameData.get(GameQClasses.games.users.nickname);
        String profileImageUrl = gameData.get(8, String.class);
        boolean isPrivate = Boolean.TRUE.equals(gameData.get(GameQClasses.games.isNamePrivate));

        if (isPrivate) {
            nickname = GameConstants.ANONYMOUS_NICKNAME;
            profileImageUrl = null;
        }

        return GameDetailResponse.builder()
                .title(gameData.get(GameQClasses.games.title))
                .description(gameData.get(GameQClasses.games.description))
                .categories(categories)
                .existsBlind(gameData.get(GameQClasses.games.isBlind))
                .existsMine(Boolean.TRUE.equals(gameData.get(9, Boolean.class)))
                .totalPlayNums(commonGameRepository.safeIntValue(gameData.get(10, Long.class)))
                .totalResourceNums(commonGameRepository.safeIntValue(gameData.get(11, Long.class)))
                .createdAt(gameData.get(GameQClasses.games.createdDate))
                .updatedAt(gameData.get(GameQClasses.games.updatedDate))
                .userResponse(UserMainResponse.builder()
                        .nickname(nickname)
                        .profileImageUrl(profileImageUrl)
                        .build())
                .leftSelection(!selections.isEmpty() ? selections.get(0) : null)
                .rightSelection(selections.size() > 1 ? selections.get(1) : null)
                .build();
    }

    // =========================== 정렬 및 페이징 ===========================

    private List<GameListResponse> applySorting(List<GameListResponse> responses, GameSortType sortType) {
        return switch (sortType) {
            case OLD -> responses.stream()
                    .sorted(Comparator.comparing(GameListResponse::getRoomId))
                    .collect(Collectors.toList());
            case RECENT -> responses.stream()
                    .sorted(Comparator.comparing(GameListResponse::getRoomId).reversed())
                    .collect(Collectors.toList());
            case WEEK -> responses.stream()
                    .sorted((r1, r2) -> {
                        int weekCompare = Integer.compare(r2.getWeekPlayNums(), r1.getWeekPlayNums());
                        return weekCompare != 0 ? weekCompare : Long.compare(r2.getRoomId(), r1.getRoomId());
                    })
                    .collect(Collectors.toList());
            case MONTH -> responses.stream()
                    .sorted(Comparator.comparingInt(GameListResponse::getMonthPlayNums).reversed()
                            .thenComparing(Comparator.comparing(GameListResponse::getRoomId).reversed()))
                    .collect(Collectors.toList());
            case PLAY_DESC -> responses.stream()
                    .sorted(Comparator.comparingInt(GameListResponse::getTotalPlayNums).reversed()
                            .thenComparing(Comparator.comparing(GameListResponse::getRoomId).reversed()))
                    .collect(Collectors.toList());
        };
    }

    private List<GameListResponse> applyPaging(List<GameListResponse> sortedResponses, Long cursorId, Pageable pageable) {
        return commonGameRepository.applyCursorPagingWithCustomCursor(
                sortedResponses, cursorId, GameListResponse::getRoomId, pageable);
    }

    // =========================== 카테고리 관련 ===========================

    private Map<Category, Long> fetchCategoryCounts(BooleanBuilder conditions) {
        Map<Category, Long> counts = new EnumMap<>(Category.class);
        Arrays.stream(Category.values()).forEach(cat -> counts.put(cat, GameConstants.DEFAULT_COUNT));

        List<Tuple> result = jpaQueryFactory
                .select(GameQClasses.category.category, GameQClasses.games.id.countDistinct())
                .from(GameQClasses.games)
                .join(GameQClasses.games.categories, GameQClasses.category)
                .join(GameQClasses.games.users, GameQClasses.users)
                .leftJoin(GameQClasses.games.gameResources, GameQClasses.resources)
                .where(conditions)
                .groupBy(GameQClasses.category.category)
                .having(GameQClasses.games.gameResources.size().goe(GameConstants.MIN_RESOURCE_COUNT))
                .fetch();

        result.forEach(tuple -> {
            Category cat = tuple.get(GameQClasses.category.category);
            Long count = Optional.ofNullable(tuple.get(GameQClasses.games.id.countDistinct()))
                    .orElse(GameConstants.DEFAULT_COUNT);
            if (cat != null) {
                counts.put(cat, count);
            }
        });

        return counts;
    }

    private GameCategoryNumsResponse createEmptyCategoryResponse() {
        Map<Category, Long> emptyCounts = new EnumMap<>(Category.class);
        Arrays.stream(Category.values()).forEach(cat -> emptyCounts.put(cat, GameConstants.DEFAULT_COUNT));

        return GameCategoryNumsResponse.builder()
                .totalNums(0)
                .categoryNums(emptyCounts)
                .build();
    }

    // =========================== 총 개수 계산 ===========================

    private Long calculateTotalElements(GameSearchRequest searchRequest, Users user, GameListType listType) {
        BooleanBuilder totalBuilder = buildGameListConditions(searchRequest, user, listType);

        return (long) jpaQueryFactory
            .selectFrom(GameQClasses.games)
            .leftJoin(GameQClasses.results).on(GameQClasses.results.gameResources.games.eq(GameQClasses.games))
            .leftJoin(GameQClasses.games.gameResources, GameQClasses.resources)
            .leftJoin(GameQClasses.games.categories, GameQClasses.category)
            .leftJoin(GameQClasses.games.users, GameQClasses.users)
            .where(totalBuilder)
            .groupBy(GameQClasses.games.id)
            .having(GameQClasses.games.gameResources.size().goe(2))
            .fetch()
            .size();
    }
}
