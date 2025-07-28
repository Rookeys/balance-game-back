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
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
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

        // 기본 쿼리 (정렬 없이)
        List<Tuple> allTuples = jpaQueryFactory
                .select(QEntities.games.id,                                     // 0
                        QEntities.games.title,                                  // 1
                        QEntities.games.description,                            // 2
                        QEntities.games.users.nickname,                         // 3
                        QEntities.images.fileUrl.max(),                         // 4
                        QEntities.games.isNamePrivate,                          // 5
                        QEntities.games.createdDate,                            // 6
                        QEntities.games.isBlind,                                // 7
                        context.getExistsMineExpression())                      // 8
                .from(QEntities.games)
                .leftJoin(QEntities.games.users, QEntities.users)
                .leftJoin(QEntities.images).on(QEntities.images.users.uid.eq(QEntities.users.uid))
                .leftJoin(QEntities.games.gameResources, QEntities.resources)
                .leftJoin(QEntities.games.categories, QEntities.category)
                .where(conditions)
                .groupBy(QEntities.games.id)
                .having(QEntities.resources.count().goe(Constants.MIN_RESOURCE_COUNT))
                .fetch();

        // GameListResponse 생성
        List<GameListResponse> allResponses = buildGameListResponses(allTuples, context.getUser());

        // Java에서 정렬
        List<GameListResponse> sortedResponses = applySorting(allResponses, context.getSortType());

        // 커서 페이징 적용
        return applyCursorPaging(sortedResponses, context.getCursorId(), context.getSortType(), pageable);
    }

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
                        // 주간 플레이 횟수로 내림차순 정렬
                        int weekCompare = Integer.compare(r2.getWeekPlayNums(), r1.getWeekPlayNums());
                        if (weekCompare != 0) {
                            return weekCompare;
                        }
                        // 동일하면 ID로 내림차순
                        return Long.compare(r2.getRoomId(), r1.getRoomId());
                    })
                    .collect(Collectors.toList());
            case MONTH -> responses.stream()
                    .sorted(Comparator.<GameListResponse>comparingInt(GameListResponse::getTotalPlayNums).reversed()
                            .thenComparing(Comparator.comparing(GameListResponse::getRoomId).reversed()))
                    .collect(Collectors.toList());
            case PLAY_DESC -> responses.stream()
                    .sorted(Comparator.<GameListResponse>comparingInt(GameListResponse::getTotalPlayNums).reversed()
                            .thenComparing(Comparator.comparing(GameListResponse::getRoomId).reversed()))
                    .collect(Collectors.toList());
            default -> responses;
        };
    }

    private List<GameListResponse> applyCursorPaging(List<GameListResponse> sortedResponses, Long cursorId,
                                                     GameSortType sortType, Pageable pageable) {

        if (cursorId == null) {
            // 첫 페이지
            return sortedResponses.stream()
                    .limit(pageable.getPageSize() + 1)
                    .collect(Collectors.toList());
        }

        // 커서 위치 찾기
        int cursorIndex = -1;
        for (int i = 0; i < sortedResponses.size(); i++) {
            if (sortedResponses.get(i).getRoomId().equals(cursorId)) {
                cursorIndex = i;
                break;
            }
        }

        if (cursorIndex == -1) {
            log.warn("Cursor game not found: {}", cursorId);
            return Collections.emptyList();
        }

        // 커서 다음부터 페이지 크기만큼 반환
        return sortedResponses.stream()
                .skip(cursorIndex + 1)
                .limit(pageable.getPageSize() + 1)
                .collect(Collectors.toList());
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

    // =========================== 응답 생성 관련 ===========================

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
        Map<Long, List<GameListSelectionResponse>> selectionsMap = getTopResourcesBatch(gameIds);
        Map<Long, GamePlayCounts> playCountsMap = getPlayCountsBatch(gameIds);

        return resultTuples.stream()
                .map(tuple -> buildGameListResponse(tuple, user, categoriesMap, selectionsMap, playCountsMap))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private GameListResponse buildGameListResponse(Tuple tuple, Users user,
                                                   Map<Long, List<Category>> categoriesMap,
                                                   Map<Long, List<GameListSelectionResponse>> selectionsMap,
                                                   Map<Long, GamePlayCounts> playCountsMap) {
        Long roomId = tuple.get(0, Long.class);
        if (roomId == null) return null;

        String nickname = tuple.get(3, String.class);
        String profileImageUrl = tuple.get(4, String.class);
        boolean isPrivate = Boolean.TRUE.equals(tuple.get(5, Boolean.class));

        if (isPrivate) {
            nickname = Constants.ANONYMOUS_NICKNAME;
            profileImageUrl = null;
        }

        List<Category> categories = categoriesMap.getOrDefault(roomId, Collections.emptyList());
        List<GameListSelectionResponse> selections = selectionsMap.getOrDefault(roomId, Collections.emptyList());
        GamePlayCounts counts = playCountsMap.getOrDefault(roomId, new GamePlayCounts(0, 0, 0));

        Boolean existsMineResult = tuple.get(8, Boolean.class);
        boolean existsMine = Boolean.TRUE.equals(existsMineResult);

        return GameListResponse.builder()
                .roomId(roomId)
                .title(tuple.get(1, String.class))
                .description(tuple.get(2, String.class))
                .categories(categories)
                .existsBlind(tuple.get(7, Boolean.class))
                .existsMine(existsMine)
                .totalPlayNums(counts.totalPlays())
                .weekPlayNums(counts.weekPlays())
                .monthPlayNums(counts.monthPlays())
                .createdAt(tuple.get(6, OffsetDateTime.class))
                .userResponse(UserMainResponse.builder()
                        .nickname(nickname)
                        .profileImageUrl(profileImageUrl)
                        .build())
                .leftSelection(!selections.isEmpty() ? selections.get(0) : null)
                .rightSelection(selections.size() > 1 ? selections.get(1) : null)
                .build();
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
                .orderBy(QEntities.resources.games.id.asc(),
                        QEntities.resources.winningLists.size().desc(),
                        QEntities.resources.id.desc())
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

    private Map<Long, GamePlayCounts> getPlayCountsBatch(List<Long> gameIds) {
        if (gameIds.isEmpty()) {
            return Collections.emptyMap();
        }

        OffsetDateTime oneWeekAgo = OffsetDateTime.now().minusWeeks(1);
        OffsetDateTime oneMonthAgo = OffsetDateTime.now().minusMonths(1);

        // 각 게임별로 총 플레이 횟수, 주간 플레이 횟수, 월간 플레이 횟수를 조회
        Map<Long, GamePlayCounts> playCountsMap = new HashMap<>();

        // 총 플레이 횟수 조회
        List<Tuple> totalResults = jpaQueryFactory
                .select(QEntities.games.id, QEntities.results.count())
                .from(QEntities.games)
                .leftJoin(QEntities.games.gameResources, QEntities.resources)
                .leftJoin(QEntities.results).on(QEntities.results.gameResources.eq(QEntities.resources))
                .where(QEntities.games.id.in(gameIds)
                        .and(QEntities.games.accessType.eq(AccessType.PUBLIC)))
                .groupBy(QEntities.games.id)
                .having(QEntities.resources.count().goe(Constants.MIN_RESOURCE_COUNT))
                .fetch();

        // 주간 플레이 횟수 조회
        List<Tuple> weekResults = jpaQueryFactory
                .select(QEntities.games.id, QEntities.results.count())
                .from(QEntities.games)
                .leftJoin(QEntities.games.gameResources, QEntities.resources)
                .leftJoin(QEntities.results).on(QEntities.results.gameResources.eq(QEntities.resources)
                        .and(QEntities.results.createdDate.after(oneWeekAgo)))
                .where(QEntities.games.id.in(gameIds)
                        .and(QEntities.games.accessType.eq(AccessType.PUBLIC)))
                .groupBy(QEntities.games.id)
                .having(QEntities.resources.count().goe(Constants.MIN_RESOURCE_COUNT))
                .fetch();

        // 월간 플레이 횟수 조회
        List<Tuple> monthResults = jpaQueryFactory
                .select(QEntities.games.id, QEntities.results.count())
                .from(QEntities.games)
                .leftJoin(QEntities.games.gameResources, QEntities.resources)
                .leftJoin(QEntities.results).on(QEntities.results.gameResources.eq(QEntities.resources)
                        .and(QEntities.results.createdDate.after(oneMonthAgo)))
                .where(QEntities.games.id.in(gameIds)
                        .and(QEntities.games.accessType.eq(AccessType.PUBLIC)))
                .groupBy(QEntities.games.id)
                .having(QEntities.resources.count().goe(Constants.MIN_RESOURCE_COUNT))
                .fetch();

        // 총 플레이 횟수 매핑
        Map<Long, Integer> totalCountMap = totalResults.stream()
                .filter(tuple -> tuple.get(QEntities.games.id) != null)
                .collect(Collectors.toMap(
                        tuple -> tuple.get(QEntities.games.id),
                        tuple -> safeIntValue(tuple.get(1, Long.class))
                ));

        // 주간 플레이 횟수 매핑
        Map<Long, Integer> weekCountMap = weekResults.stream()
                .filter(tuple -> tuple.get(QEntities.games.id) != null)
                .collect(Collectors.toMap(
                        tuple -> tuple.get(QEntities.games.id),
                        tuple -> safeIntValue(tuple.get(1, Long.class))
                ));

        // 월간 플레이 횟수 매핑
        Map<Long, Integer> monthCountMap = monthResults.stream()
                .filter(tuple -> tuple.get(QEntities.games.id) != null)
                .collect(Collectors.toMap(
                        tuple -> tuple.get(QEntities.games.id),
                        tuple -> safeIntValue(tuple.get(1, Long.class))
                ));

        // 최종 결과 생성
        for (Long gameId : gameIds) {
            int totalCount = totalCountMap.getOrDefault(gameId, 0);
            int weekCount = weekCountMap.getOrDefault(gameId, 0);
            int monthCount = monthCountMap.getOrDefault(gameId, 0);
            playCountsMap.put(gameId, new GamePlayCounts(totalCount, weekCount, monthCount));
        }

        return playCountsMap;
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

    private Long calculateTotalElements(GameListQueryContext context) {
        BooleanBuilder totalBuilder = this.buildGameListConditions(context);

        List<Long> gameIds = jpaQueryFactory
                .select(QEntities.games.id)
                .from(QEntities.games)
                .leftJoin(QEntities.games.users, QEntities.users)
                .leftJoin(QEntities.games.gameResources, QEntities.resources)
                .leftJoin(QEntities.games.categories, QEntities.category)
                .where(totalBuilder)
                .groupBy(QEntities.games.id)
                .having(QEntities.resources.count().goe(Constants.MIN_RESOURCE_COUNT))
                .fetch();

        return (long) gameIds.size();
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

    // =========================== 내부 클래스 및 데이터 클래스 ===========================

    private record GamePlayCounts(int totalPlays, int weekPlays, int monthPlays) {}

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