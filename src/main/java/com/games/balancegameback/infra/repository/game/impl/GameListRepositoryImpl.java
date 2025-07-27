package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.core.exception.impl.UnAuthorizedException;
import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.enums.AccessType;
import com.games.balancegameback.domain.game.enums.Category;
import com.games.balancegameback.domain.game.enums.GameSortType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.*;
import com.games.balancegameback.dto.user.UserMainResponse;
import com.games.balancegameback.infra.entity.*;
import com.games.balancegameback.service.game.repository.GameListRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Expression;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GameListRepositoryImpl implements GameListRepository {

    private final JPAQueryFactory jpaQueryFactory;

    // 상수 클래스
    private static class Constants {
        static final int MIN_RESOURCE_COUNT = 2;
        static final int TOP_RESOURCE_LIMIT = 2;
        static final String ANONYMOUS_NICKNAME = "익명";
        static final long DEFAULT_COUNT = 0L;
        static final int DEFAULT_SEC = 0;
    }

    // Q클래스 홀더
    private static class QEntities {
        static final QGamesEntity games = QGamesEntity.gamesEntity;
        static final QUsersEntity users = QUsersEntity.usersEntity;
        static final QGameResourcesEntity resources = QGameResourcesEntity.gameResourcesEntity;
        static final QGameCategoryEntity category = QGameCategoryEntity.gameCategoryEntity;
        static final QGameResultsEntity results = QGameResultsEntity.gameResultsEntity;
        static final QImagesEntity images = QImagesEntity.imagesEntity;
        static final QLinksEntity links = QLinksEntity.linksEntity;
    }

    // 정렬 전략 패턴
    private interface SortStrategy {
        OrderSpecifier<?> getOrderSpecifier();
        NumberExpression<Long> getPlayCountExpression();
        BooleanExpression getTimeCondition();
        String getSortType();
    }

    private final Map<GameSortType, SortStrategy> sortStrategies = createSortStrategies();

    private Map<GameSortType, SortStrategy> createSortStrategies() {
        OffsetDateTime now = OffsetDateTime.now();

        Map<GameSortType, SortStrategy> strategies = new EnumMap<>(GameSortType.class);

        strategies.put(GameSortType.OLD, new SortStrategy() {
            public OrderSpecifier<?> getOrderSpecifier() { return QEntities.games.id.asc(); }
            public NumberExpression<Long> getPlayCountExpression() { return Expressions.asNumber(Constants.DEFAULT_COUNT); }
            public BooleanExpression getTimeCondition() { return null; }
            public String getSortType() { return "OLD"; }
        });

        strategies.put(GameSortType.RECENT, new SortStrategy() {
            public OrderSpecifier<?> getOrderSpecifier() { return QEntities.games.id.desc(); }
            public NumberExpression<Long> getPlayCountExpression() { return Expressions.asNumber(Constants.DEFAULT_COUNT); }
            public BooleanExpression getTimeCondition() { return null; }
            public String getSortType() { return "RECENT"; }
        });

        strategies.put(GameSortType.WEEK, new SortStrategy() {
            public OrderSpecifier<?> getOrderSpecifier() { return getPlayCountExpression().desc().nullsLast(); }
            public NumberExpression<Long> getPlayCountExpression() {
                // 조건부 카운트: 지난 주 내의 결과만 카운트
                return QEntities.results.createdDate.after(now.minusWeeks(1)).count();
            }
            public BooleanExpression getTimeCondition() {
                // 조인 조건은 null로 설정 - 조건부 카운트를 사용하므로
                return null;
            }
            public String getSortType() { return "WEEK"; }
        });

        strategies.put(GameSortType.MONTH, new SortStrategy() {
            public OrderSpecifier<?> getOrderSpecifier() { return getPlayCountExpression().desc().nullsLast(); }
            public NumberExpression<Long> getPlayCountExpression() {
                // 조건부 카운트: 지난 달 내의 결과만 카운트
                return QEntities.results.createdDate.after(now.minusMonths(1)).count();
            }
            public BooleanExpression getTimeCondition() {
                // 조인 조건은 null로 설정 - 조건부 카운트를 사용하므로
                return null;
            }
            public String getSortType() { return "MONTH"; }
        });

        strategies.put(GameSortType.PLAY_DESC, new SortStrategy() {
            public OrderSpecifier<?> getOrderSpecifier() { return getPlayCountExpression().desc().nullsLast(); }
            public NumberExpression<Long> getPlayCountExpression() { return QEntities.results.count(); }
            public BooleanExpression getTimeCondition() { return null; }
            public String getSortType() { return "PLAY_DESC"; }
        });

        return Collections.unmodifiableMap(strategies);
    }

    @Override
    public GameCategoryNumsResponse getCategoryCounts(String title) {
        try {
            BooleanBuilder conditions = createBaseCategoryConditions(title);
            Map<Category, Long> counts = fetchCategoryCounts(conditions);
            int total = calculateTotalCount(counts);

            return GameCategoryNumsResponse.builder()
                    .totalNums(total)
                    .categoryNums(counts)
                    .build();
        } catch (Exception e) {
            log.error("Error fetching category counts for title: {}", title, e);
            return createEmptyCategoryResponse();
        }
    }

    @Override
    public GameDetailResponse getGameStatus(Long gameId, Users user) {
        validateGameAccess(gameId, user);

        try {
            GameDetailData gameData = fetchGameDetailData(gameId, user);
            List<Category> categories = fetchGameCategories(gameId);
            List<GameListSelectionResponse> topSelections = fetchTopSelections(gameId);

            return buildGameDetailResponse(gameData, categories, topSelections);
        } catch (Exception e) {
            log.error("Error fetching game detail for gameId: {}", gameId, e);
            throw new NotFoundException("Game detail fetch failed", ErrorCode.NOT_FOUND_EXCEPTION);
        }
    }

    @Override
    public CustomPageImpl<GameListResponse> getGameList(Long cursorId, Pageable pageable,
                                                        GameSearchRequest searchRequest, Users users) {
        try {
            GameListQueryContext context = new GameListQueryContext(cursorId, searchRequest, users);
            List<GameListResponse> gameResponses = executeGameListQuery(context, pageable);

            boolean hasNext = gameResponses.size() > pageable.getPageSize();
            if (hasNext) {
                gameResponses.removeLast();
            }

            Long totalElements = calculateTotalElements(context);

            return new CustomPageImpl<>(gameResponses, pageable, totalElements, cursorId, hasNext);
        } catch (Exception e) {
            log.error("Error fetching game list with cursor: {}, search: {}", cursorId, searchRequest, e);
            return new CustomPageImpl<>(Collections.emptyList(), pageable, 0L, cursorId, false);
        }
    }

    // =========================== 카테고리 조회 관련 ===========================

    private BooleanBuilder createBaseCategoryConditions(String title) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QEntities.games.accessType.eq(AccessType.PUBLIC));

        if (StringUtils.hasText(title)) {
            String searchTitle = title.trim();
            BooleanExpression searchCondition = QEntities.games.title.containsIgnoreCase(searchTitle)
                    .or(QEntities.resources.title.containsIgnoreCase(searchTitle))
                    .or(QEntities.users.nickname.containsIgnoreCase(searchTitle)
                            .and(QEntities.games.isNamePrivate.eq(false)));
            builder.and(searchCondition);
        }

        return builder;
    }

    private Map<Category, Long> fetchCategoryCounts(BooleanBuilder conditions) {
        Map<Category, Long> counts = new EnumMap<>(Category.class);
        Arrays.stream(Category.values()).forEach(cat -> counts.put(cat, Constants.DEFAULT_COUNT));

        List<Tuple> result = jpaQueryFactory
                .select(QEntities.category.category, QEntities.games.id.countDistinct())
                .from(QEntities.games)
                .join(QEntities.games.categories, QEntities.category)
                .join(QEntities.games.users, QEntities.users)
                .leftJoin(QEntities.games.gameResources, QEntities.resources)
                .where(conditions)
                .groupBy(QEntities.category.category)
                .having(QEntities.games.gameResources.size().goe(Constants.MIN_RESOURCE_COUNT))
                .fetch();

        result.forEach(tuple -> {
            Category cat = tuple.get(QEntities.category.category);
            Long count = Optional.ofNullable(tuple.get(QEntities.games.id.countDistinct()))
                    .orElse(Constants.DEFAULT_COUNT);
            if (cat != null) {
                counts.put(cat, count);
            }
        });

        return counts;
    }

    private int calculateTotalCount(Map<Category, Long> counts) {
        return counts.values().stream()
                .filter(Objects::nonNull)
                .mapToInt(Long::intValue)
                .sum();
    }

    private GameCategoryNumsResponse createEmptyCategoryResponse() {
        Map<Category, Long> emptyCounts = new EnumMap<>(Category.class);
        Arrays.stream(Category.values()).forEach(cat -> emptyCounts.put(cat, Constants.DEFAULT_COUNT));

        return GameCategoryNumsResponse.builder()
                .totalNums(0)
                .categoryNums(emptyCounts)
                .build();
    }

    // =========================== 게임 상세 조회 관련 ===========================

    private void validateGameAccess(Long gameId, Users user) {
        if (!isAccessibleByUser(gameId, user)) {
            throw new UnAuthorizedException("접근 불가", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }
    }

    private GameDetailData fetchGameDetailData(Long gameId, Users user) {
        Expression<Boolean> existsMineExpr = user != null ?
                QEntities.games.users.uid.eq(user.getUid()) : Expressions.FALSE;

        Tuple gameData = jpaQueryFactory
                .select(QEntities.games.id, QEntities.games.title, QEntities.games.description,
                        QEntities.games.users.nickname, QEntities.games.isNamePrivate,
                        QEntities.games.createdDate, QEntities.games.updatedDate, QEntities.games.isBlind,
                        QEntities.images.fileUrl.max(), existsMineExpr,
                        QEntities.results.count().coalesce(Constants.DEFAULT_COUNT).as("totalPlays"),
                        QEntities.resources.count().coalesce(Constants.DEFAULT_COUNT).as("totalResources"))
                .from(QEntities.games)
                .leftJoin(QEntities.games.users, QEntities.users)
                .leftJoin(QEntities.images).on(QEntities.images.users.uid.eq(QEntities.users.uid))
                .leftJoin(QEntities.games.gameResources, QEntities.resources)
                .leftJoin(QEntities.results).on(QEntities.results.gameResources.eq(QEntities.resources))
                .where(QEntities.games.id.eq(gameId))
                .groupBy(QEntities.games.id)
                .having(QEntities.resources.count().goe(Constants.MIN_RESOURCE_COUNT))
                .fetchOne();

        if (gameData == null) {
            throw new NotFoundException("Game not found", ErrorCode.NOT_FOUND_EXCEPTION);
        }

        return new GameDetailData(gameData, user);
    }

    private List<Category> fetchGameCategories(Long gameId) {
        return jpaQueryFactory
                .select(QEntities.category.category)
                .from(QEntities.category)
                .where(QEntities.category.games.id.eq(gameId))
                .fetch();
    }

    private List<GameListSelectionResponse> fetchTopSelections(Long gameId) {
        List<Tuple> topResources = jpaQueryFactory
                .select(QEntities.resources.id, QEntities.resources.title,
                        QEntities.images.fileUrl.coalesce(QEntities.links.urls),
                        QEntities.images.mediaType.coalesce(QEntities.links.mediaType),
                        QEntities.links.startSec.coalesce(Constants.DEFAULT_SEC),
                        QEntities.links.endSec.coalesce(Constants.DEFAULT_SEC))
                .from(QEntities.resources)
                .leftJoin(QEntities.resources.images, QEntities.images)
                .leftJoin(QEntities.resources.links, QEntities.links)
                .where(QEntities.resources.games.id.eq(gameId))
                .orderBy(QEntities.resources.winningLists.size().desc(), QEntities.resources.id.desc())
                .limit(Constants.TOP_RESOURCE_LIMIT)
                .fetch();

        return topResources.stream()
                .map(this::buildSelectionResponse)
                .collect(Collectors.toList());
    }

    private GameDetailResponse buildGameDetailResponse(GameDetailData gameData, List<Category> categories,
                                                       List<GameListSelectionResponse> selections) {
        return GameDetailResponse.builder()
                .title(gameData.getTitle())
                .description(gameData.getDescription())
                .categories(categories)
                .existsBlind(gameData.isBlind())
                .existsMine(gameData.isMine())
                .totalPlayNums(gameData.getTotalPlays())
                .totalResourceNums(gameData.getTotalResources())
                .createdAt(gameData.getCreatedDate())
                .updatedAt(gameData.getUpdatedDate())
                .userResponse(gameData.getUserResponse())
                .leftSelection(!selections.isEmpty() ? selections.get(0) : null)
                .rightSelection(selections.size() > 1 ? selections.get(1) : null)
                .build();
    }

    // =========================== 게임 리스트 조회 관련 ===========================

    private List<GameListResponse> executeGameListQuery(GameListQueryContext context, Pageable pageable) {
        BooleanBuilder conditions = buildGameListConditions(context);
        SortStrategy sortStrategy = sortStrategies.get(context.getSortType());

        // 커서 조건 적용 - 수정된 부분
        if (context.getCursorId() != null) {
            BooleanExpression cursorCondition = createCursorCondition(context.getCursorId(), sortStrategy, conditions);
            if (cursorCondition != null) {
                conditions.and(cursorCondition);
                log.debug("Applied cursor condition for gameId: {} with sortType: {}",
                        context.getCursorId(), sortStrategy.getSortType());
            }
        }

        // 시간 조건에 따른 results join 처리 - 수정됨
        BooleanExpression timeCondition = sortStrategy.getTimeCondition();

        List<Tuple> resultTuples = jpaQueryFactory
                .select(QEntities.games.id,                                     // 0
                        QEntities.games.title,                                  // 1
                        QEntities.games.description,                            // 2
                        QEntities.games.users.nickname,                         // 3
                        QEntities.images.fileUrl.max(),                         // 4
                        QEntities.games.isNamePrivate,                          // 5
                        QEntities.games.createdDate,                            // 6
                        QEntities.games.isBlind,                                // 7
                        context.getExistsMineExpression(),                      // 8
                        sortStrategy.getPlayCountExpression().as("playCount"))  // 9
                .from(QEntities.games)
                .leftJoin(QEntities.games.users, QEntities.users)
                .leftJoin(QEntities.images).on(QEntities.images.users.uid.eq(QEntities.users.uid))
                .leftJoin(QEntities.games.gameResources, QEntities.resources)
                .leftJoin(QEntities.results).on(QEntities.results.gameResources.eq(QEntities.resources))
                .leftJoin(QEntities.games.categories, QEntities.category)
                .where(conditions)
                .groupBy(QEntities.games.id)
                .having(QEntities.resources.count().goe(Constants.MIN_RESOURCE_COUNT))
                .orderBy(sortStrategy.getOrderSpecifier(), QEntities.games.id.desc())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        log.debug("Query returned {} results for cursor: {}", resultTuples.size(), context.getCursorId());
        return buildGameListResponses(resultTuples, context.getUser());
    }

    private BooleanBuilder buildGameListConditions(GameListQueryContext context) {
        BooleanBuilder builder = new BooleanBuilder();

        // 기본 조건
        builder.and(QEntities.games.accessType.eq(AccessType.PUBLIC));

        // 카테고리 필터
        if (context.getCategory() != null) {
            builder.and(QEntities.category.category.eq(context.getCategory()));
        }

        // 제목 검색
        if (StringUtils.hasText(context.getTitle())) {
            String searchTitle = context.getTitle().trim();
            BooleanExpression searchCondition = QEntities.games.title.containsIgnoreCase(searchTitle)
                    .or(QEntities.resources.title.containsIgnoreCase(searchTitle))
                    .or(QEntities.users.nickname.containsIgnoreCase(searchTitle)
                            .and(QEntities.games.isNamePrivate.eq(false)));
            builder.and(searchCondition);
        }

        return builder;
    }

    // =========================== 커서 페이징 관련 - 완전히 새로 작성 ===========================

    /**
     * 커서 기반 조건을 생성합니다.
     * 커서 ID에 해당하는 게임의 정렬 기준값을 조회한 후,
     * 해당 기준값보다 낮은 순위의 게임들만 조회하도록 조건을 생성합니다.
     */
    private BooleanExpression createCursorCondition(Long cursorId, SortStrategy sortStrategy, BooleanBuilder baseConditions) {
        String sortType = sortStrategy.getSortType();

        if ("OLD".equals(sortType)) {
            return QEntities.games.id.gt(cursorId);
        } else if ("RECENT".equals(sortType)) {
            return QEntities.games.id.lt(cursorId);
        }

        // 플레이 횟수 기반 정렬의 경우
        return createPlayCountBasedCursor(cursorId, sortStrategy, baseConditions);
    }

    /**
     * 플레이 횟수 기반 커서 조건을 생성합니다.
     */
    private BooleanExpression createPlayCountBasedCursor(Long cursorId, SortStrategy sortStrategy, BooleanBuilder baseConditions) {
        // 1. 커서 게임의 플레이 횟수를 정확히 조회 (동일한 조건으로)
        Long cursorPlayCount = getCursorGamePlayCountWithConditions(cursorId, sortStrategy, baseConditions);
        if (cursorPlayCount == null) {
            log.warn("Could not find play count for cursor game: {}", cursorId);
            return QEntities.games.id.lt(cursorId); // fallback
        }

        log.debug("Cursor game {} has play count: {} for sort type: {}", cursorId, cursorPlayCount, sortStrategy.getSortType());

        // 2. 커서보다 낮은 순위 조건 생성
        // 플레이 횟수가 더 적거나, 같다면 ID가 더 작은 게임들
        BooleanExpression lowerPlayCount = sortStrategy.getPlayCountExpression().lt(cursorPlayCount);
        BooleanExpression samePlayCountLowerId = sortStrategy.getPlayCountExpression().eq(cursorPlayCount)
                .and(QEntities.games.id.lt(cursorId));

        return lowerPlayCount.or(samePlayCountLowerId);
    }

    /**
     * 특정 게임의 플레이 횟수를 메인 쿼리와 동일한 조건으로 조회합니다.
     */
    private Long getCursorGamePlayCountWithConditions(Long gameId, SortStrategy sortStrategy, BooleanBuilder baseConditions) {
        try {
            BooleanExpression timeCondition = sortStrategy.getTimeCondition();

            // 메인 쿼리와 동일한 조건으로 커서 게임의 플레이 횟수 조회
            BooleanBuilder cursorConditions = new BooleanBuilder();
            cursorConditions.and(QEntities.games.id.eq(gameId));
            cursorConditions.and(QEntities.games.accessType.eq(AccessType.PUBLIC));

            JPAQuery<Long> query = jpaQueryFactory
                    .select(sortStrategy.getPlayCountExpression())
                    .from(QEntities.games)
                    .leftJoin(QEntities.games.users, QEntities.users)
                    .leftJoin(QEntities.games.gameResources, QEntities.resources)
                    .leftJoin(QEntities.results).on(QEntities.results.gameResources.eq(QEntities.resources))
                    .leftJoin(QEntities.games.categories, QEntities.category)
                    .where(cursorConditions)
                    .groupBy(QEntities.games.id)
                    .having(QEntities.resources.count().goe(Constants.MIN_RESOURCE_COUNT));

            Long playCount = query.fetchOne();
            return playCount != null ? playCount : 0L;
        } catch (Exception e) {
            log.error("Error fetching play count for game: {}", gameId, e);
            return null;
        }
    }

    /**
     * 특정 게임의 플레이 횟수를 조회합니다. (기존 메서드)
     */
    private Long getCursorGamePlayCount(Long gameId, SortStrategy sortStrategy) {
        try {
            BooleanExpression timeCondition = sortStrategy.getTimeCondition();

            JPAQuery<Long> query = jpaQueryFactory
                    .select(sortStrategy.getPlayCountExpression())
                    .from(QEntities.games)
                    .leftJoin(QEntities.games.gameResources, QEntities.resources)
                    .leftJoin(QEntities.results).on(
                            timeCondition != null ?
                                    QEntities.results.gameResources.eq(QEntities.resources).and(timeCondition) :
                                    QEntities.results.gameResources.eq(QEntities.resources)
                    )
                    .where(QEntities.games.id.eq(gameId)
                            .and(QEntities.games.accessType.eq(AccessType.PUBLIC)))
                    .groupBy(QEntities.games.id)
                    .having(QEntities.resources.count().goe(Constants.MIN_RESOURCE_COUNT));

            Long playCount = query.fetchOne();
            return playCount != null ? playCount : 0L;
        } catch (Exception e) {
            log.error("Error fetching play count for game: {}", gameId, e);
            return null;
        }
    }

    // =========================== 배치 조회 및 응답 생성 ===========================

    private List<GameListResponse> buildGameListResponses(List<Tuple> resultTuples, Users user) {
        if (resultTuples.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> gameIds = resultTuples.stream()
                .map(tuple -> tuple.get(QEntities.games.id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        // 배치 조회로 N+1 문제 해결
        Map<Long, List<Category>> categoriesMap = getCategoriesBatch(gameIds);
        // GameStats는 메인 쿼리의 결과를 직접 사용하도록 수정
        Map<Long, List<GameListSelectionResponse>> selectionsMap = getTopResourcesBatch(gameIds);

        return resultTuples.stream()
                .map(tuple -> buildGameListResponse(tuple, user, categoriesMap, selectionsMap))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private GameListResponse buildGameListResponse(Tuple tuple, Users user,
                                                   Map<Long, List<Category>> categoriesMap,
                                                   Map<Long, List<GameListSelectionResponse>> selectionsMap) {
        Long roomId = tuple.get(0, Long.class);        // games.id
        if (roomId == null) return null;

        String nickname = tuple.get(3, String.class);  // users.nickname
        String profileImageUrl = tuple.get(4, String.class); // max(images.fileUrl)
        boolean isPrivate = Boolean.TRUE.equals(tuple.get(5, Boolean.class)); // isNamePrivate

        if (isPrivate) {
            nickname = Constants.ANONYMOUS_NICKNAME;
            profileImageUrl = null;
        }

        List<Category> categories = categoriesMap.getOrDefault(roomId, Collections.emptyList());
        List<GameListSelectionResponse> selections = selectionsMap.getOrDefault(roomId, Collections.emptyList());

        Boolean existsMineResult = tuple.get(8, Boolean.class); // existsMineExpression
        boolean existsMine = Boolean.TRUE.equals(existsMineResult);

        // 총 플레이 횟수와 주간 플레이 횟수를 모두 정확히 계산
        GamePlayCounts counts = getGamePlayCounts(roomId);

        return GameListResponse.builder()
                .roomId(roomId)
                .title(tuple.get(1, String.class))                    // games.title
                .description(tuple.get(2, String.class))              // games.description
                .categories(categories)
                .existsBlind(tuple.get(7, Boolean.class))              // games.isBlind
                .existsMine(existsMine)
                .totalPlayNums(counts.totalPlays())
                .weekPlayNums(counts.weekPlays())
                .createdAt(tuple.get(6, OffsetDateTime.class))        // games.createdDate
                .userResponse(UserMainResponse.builder()
                        .nickname(nickname)
                        .profileImageUrl(profileImageUrl)
                        .build())
                .leftSelection(!selections.isEmpty() ? selections.get(0) : null)
                .rightSelection(selections.size() > 1 ? selections.get(1) : null)
                .build();
    }

    private Long calculateTotalElements(GameListQueryContext context) {
        BooleanBuilder totalBuilder = new BooleanBuilder();
        totalBuilder.and(QEntities.games.accessType.eq(AccessType.PUBLIC));

        if (context.getCategory() != null) {
            totalBuilder.and(QEntities.category.category.eq(context.getCategory()));
        }

        if (StringUtils.hasText(context.getTitle())) {
            String searchTitle = context.getTitle().trim();
            BooleanExpression searchCondition = QEntities.games.title.containsIgnoreCase(searchTitle)
                    .or(QEntities.resources.title.containsIgnoreCase(searchTitle))
                    .or(QEntities.users.nickname.containsIgnoreCase(searchTitle)
                            .and(QEntities.games.isNamePrivate.eq(false)));
            totalBuilder.and(searchCondition);
        }

        // 시간 조건을 JOIN에서 처리 - 수정됨
        SortStrategy sortStrategy = sortStrategies.get(context.getSortType());
        BooleanExpression timeCondition = sortStrategy.getTimeCondition();

        List<Long> gameIds = jpaQueryFactory
                .select(QEntities.games.id)
                .from(QEntities.games)
                .leftJoin(QEntities.games.users, QEntities.users)
                .leftJoin(QEntities.games.gameResources, QEntities.resources)
                .leftJoin(QEntities.games.categories, QEntities.category)
                .leftJoin(QEntities.results).on(QEntities.results.gameResources.eq(QEntities.resources))
                .where(totalBuilder)
                .groupBy(QEntities.games.id)
                .having(QEntities.resources.count().goe(Constants.MIN_RESOURCE_COUNT))
                .fetch();

        return (long) gameIds.size();
    }

    // =========================== 배치 조회 메서드 ===========================

    private Map<Long, List<Category>> getCategoriesBatch(List<Long> gameIds) {
        if (gameIds.isEmpty()) return Collections.emptyMap();

        return jpaQueryFactory
                .select(QEntities.category.games.id, QEntities.category.category)
                .from(QEntities.category)
                .where(QEntities.category.games.id.in(gameIds))
                .fetch()
                .stream()
                .filter(tuple -> tuple.get(QEntities.category.games.id) != null &&
                        tuple.get(QEntities.category.category) != null)
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(QEntities.category.games.id),
                        Collectors.mapping(
                                tuple -> tuple.get(QEntities.category.category),
                                Collectors.toList())
                ));
    }

    /**
     * 특정 게임의 총 플레이 횟수와 주간 플레이 횟수를 조회합니다.
     */
    private GamePlayCounts getGamePlayCounts(Long gameId) {
        try {
            OffsetDateTime oneWeekAgo = OffsetDateTime.now().minusWeeks(1);

            // 총 플레이 횟수 조회
            Long totalCount = jpaQueryFactory
                    .select(QEntities.results.count())
                    .from(QEntities.games)
                    .leftJoin(QEntities.games.gameResources, QEntities.resources)
                    .leftJoin(QEntities.results).on(QEntities.results.gameResources.eq(QEntities.resources))
                    .where(QEntities.games.id.eq(gameId)
                            .and(QEntities.games.accessType.eq(AccessType.PUBLIC)))
                    .groupBy(QEntities.games.id)
                    .having(QEntities.resources.count().goe(Constants.MIN_RESOURCE_COUNT))
                    .fetchOne();

            // 주간 플레이 횟수 조회 - JOIN 조건으로 필터링
            Long weekCount = jpaQueryFactory
                    .select(QEntities.results.count())
                    .from(QEntities.games)
                    .leftJoin(QEntities.games.gameResources, QEntities.resources)
                    .leftJoin(QEntities.results).on(QEntities.results.gameResources.eq(QEntities.resources)
                            .and(QEntities.results.createdDate.after(oneWeekAgo)))
                    .where(QEntities.games.id.eq(gameId)
                            .and(QEntities.games.accessType.eq(AccessType.PUBLIC)))
                    .groupBy(QEntities.games.id)
                    .having(QEntities.resources.count().goe(Constants.MIN_RESOURCE_COUNT))
                    .fetchOne();

            return new GamePlayCounts(
                    safeIntValue(totalCount),
                    safeIntValue(weekCount)
            );
        } catch (Exception e) {
            log.error("Error fetching play counts for game: {}", gameId, e);
            return new GamePlayCounts(0, 0);
        }
    }

    private record GamePlayCounts(int totalPlays, int weekPlays) {}

    private Map<Long, List<GameListSelectionResponse>> getTopResourcesBatch(List<Long> gameIds) {
        if (gameIds.isEmpty()) return Collections.emptyMap();

        List<Tuple> topResources = jpaQueryFactory
                .select(QEntities.resources.games.id, QEntities.resources.id, QEntities.resources.title,
                        QEntities.images.fileUrl.coalesce(QEntities.links.urls),
                        QEntities.images.mediaType.coalesce(QEntities.links.mediaType),
                        QEntities.links.startSec.coalesce(Constants.DEFAULT_SEC),
                        QEntities.links.endSec.coalesce(Constants.DEFAULT_SEC))
                .from(QEntities.resources)
                .leftJoin(QEntities.resources.images, QEntities.images)
                .leftJoin(QEntities.resources.links, QEntities.links)
                .where(QEntities.resources.games.id.in(gameIds))
                .orderBy(QEntities.resources.games.id.asc(), QEntities.resources.winningLists.size().desc(), QEntities.resources.id.desc())
                .fetch();

        return topResources.stream()
                .filter(tuple -> tuple.get(QEntities.resources.games.id) != null)
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(QEntities.resources.games.id),
                        LinkedHashMap::new,
                        Collectors.mapping(
                                this::buildSelectionResponse,
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> list.stream().limit(Constants.TOP_RESOURCE_LIMIT).collect(Collectors.toList())
                                )
                        )
                ));
    }

    // =========================== 헬퍼 메서드 ===========================

    private GameListSelectionResponse buildSelectionResponse(Tuple tuple) {
        return GameListSelectionResponse.builder()
                .id(tuple.get(QEntities.resources.id))
                .title(tuple.get(QEntities.resources.title))
                .type(tuple.get(QEntities.images.mediaType.coalesce(QEntities.links.mediaType)))
                .content(tuple.get(QEntities.images.fileUrl.coalesce(QEntities.links.urls)))
                .startSec(Optional.ofNullable(tuple.get(QEntities.links.startSec.coalesce(Constants.DEFAULT_SEC))).orElse(Constants.DEFAULT_SEC))
                .endSec(Optional.ofNullable(tuple.get(QEntities.links.endSec.coalesce(Constants.DEFAULT_SEC))).orElse(Constants.DEFAULT_SEC))
                .build();
    }

    private int safeIntValue(Long value) {
        return value != null ? Math.toIntExact(value) : 0;
    }

    private boolean isAccessibleByUser(Long gameId, Users user) {
        if (gameId == null) {
            return false;
        }

        try {
            BooleanExpression accessCondition = user != null ?
                    QEntities.games.accessType.ne(AccessType.PRIVATE).or(QEntities.games.users.uid.eq(user.getUid())) :
                    QEntities.games.accessType.ne(AccessType.PRIVATE);

            return jpaQueryFactory
                    .selectOne()
                    .from(QEntities.games)
                    .where(QEntities.games.id.eq(gameId).and(accessCondition))
                    .fetchFirst() != null;
        } catch (Exception e) {
            log.error("Error checking game accessibility for gameId: {}, user: {}", gameId, user, e);
            return false;
        }
    }

    // =========================== 내부 클래스 ===========================

    private record GameStats(int totalPlays, int weekPlays) {}

    private record GameDetailData(Tuple data, Users user) {

        public String getTitle() {
            return data.get(QEntities.games.title);
        }

        public String getDescription() {
            return data.get(QEntities.games.description);
        }

        public boolean isBlind() {
            return Boolean.TRUE.equals(data.get(QEntities.games.isBlind));
        }

        public boolean isMine() {
            Boolean existsMineResult = data.get(9, Boolean.class);
            return Boolean.TRUE.equals(existsMineResult);
        }

        public int getTotalPlays() {
            Long count = data.get(10, Long.class);
            return count != null ? Math.toIntExact(count) : 0;
        }

        public int getTotalResources() {
            Long count = data.get(11, Long.class);
            return count != null ? Math.toIntExact(count) : 0;
        }

        public OffsetDateTime getCreatedDate() {
            return data.get(QEntities.games.createdDate);
        }

        public OffsetDateTime getUpdatedDate() {
            return data.get(QEntities.games.updatedDate);
        }

        public UserMainResponse getUserResponse() {
            String nickname = data.get(QEntities.games.users.nickname);
            String profileImageUrl = data.get(8, String.class);
            boolean isPrivate = Boolean.TRUE.equals(data.get(QEntities.games.isNamePrivate));

            if (isPrivate) {
                nickname = Constants.ANONYMOUS_NICKNAME;
                profileImageUrl = null;
            }

            return UserMainResponse.builder()
                    .nickname(nickname)
                    .profileImageUrl(profileImageUrl)
                    .build();
        }
    }

    private static class GameListQueryContext {
        @Getter
        private final Long cursorId;
        private final GameSearchRequest searchRequest;
        @Getter
        private final Users user;
        @Getter
        private final Expression<Boolean> existsMineExpression;

        public GameListQueryContext(Long cursorId, GameSearchRequest searchRequest, Users user) {
            this.cursorId = cursorId;
            this.searchRequest = searchRequest;
            this.user = user;
            this.existsMineExpression = user != null ?
                    QEntities.games.users.uid.eq(user.getUid()) : Expressions.FALSE;
        }

        public GameSortType getSortType() { return searchRequest.getSortType(); }
        public Category getCategory() { return searchRequest.getCategory(); }
        public String getTitle() { return searchRequest.getTitle(); }
    }
}