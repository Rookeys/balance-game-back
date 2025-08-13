package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.infra.repository.game.common.CursorIdentifiable;
import com.games.balancegameback.infra.repository.game.common.GameConstants;
import com.games.balancegameback.infra.repository.game.common.GameBatchData;
import com.games.balancegameback.infra.repository.game.common.GamePlayCounts;
import com.games.balancegameback.infra.repository.game.common.GameQClasses;
import com.games.balancegameback.infra.repository.game.common.CommonGameRepository;
import com.games.balancegameback.domain.game.enums.AccessType;
import com.games.balancegameback.domain.game.enums.Category;
import com.games.balancegameback.domain.game.enums.GameSortType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.GameListResponse;
import com.games.balancegameback.dto.game.GameListSelectionResponse;
import com.games.balancegameback.dto.user.UserMainResponse;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
@Slf4j
public class CommonGameRepositoryImpl implements CommonGameRepository {

    private final JPAQueryFactory jpaQueryFactory;

    // =========================== 배치 조회 메서드들 ===========================

    @Override
    public Map<Long, List<Category>> getCategoriesBatch(List<Long> gameIds) {
        if (gameIds.isEmpty()) return Collections.emptyMap();

        return jpaQueryFactory
                .select(GameQClasses.category.games.id, GameQClasses.category.category)
                .from(GameQClasses.category)
                .where(GameQClasses.category.games.id.in(gameIds))
                .fetch()
                .stream()
                .filter(tuple -> tuple.get(GameQClasses.category.games.id) != null &&
                        tuple.get(GameQClasses.category.category) != null)
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(GameQClasses.category.games.id),
                        Collectors.mapping(
                                tuple -> tuple.get(GameQClasses.category.category),
                                Collectors.toList())
                ));
    }

    @Override
    public Map<Long, List<GameListSelectionResponse>> getTopResourcesBatch(List<Long> gameIds) {
        if (gameIds.isEmpty()) return Collections.emptyMap();

        List<Tuple> topResources = jpaQueryFactory
                .select(GameQClasses.resources.games.id, GameQClasses.resources.id, GameQClasses.resources.title,
                        GameQClasses.images.fileUrl.coalesce(GameQClasses.links.urls),
                        GameQClasses.images.mediaType.coalesce(GameQClasses.links.mediaType),
                        GameQClasses.links.startSec.coalesce(GameConstants.DEFAULT_SEC),
                        GameQClasses.links.endSec.coalesce(GameConstants.DEFAULT_SEC))
                .from(GameQClasses.resources)
                .leftJoin(GameQClasses.resources.images, GameQClasses.images)
                .leftJoin(GameQClasses.resources.links, GameQClasses.links)
                .where(GameQClasses.resources.games.id.in(gameIds))
                .orderBy(GameQClasses.resources.games.id.asc(),
                        GameQClasses.resources.winningLists.size().desc(),
                        GameQClasses.resources.id.desc())
                .fetch();

        return topResources.stream()
                .filter(tuple -> tuple.get(GameQClasses.resources.games.id) != null)
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(GameQClasses.resources.games.id),
                        LinkedHashMap::new,
                        Collectors.mapping(
                                this::buildSelectionResponse,
                                Collectors.collectingAndThen(
                                        Collectors.toList(),
                                        list -> list.stream().limit(GameConstants.TOP_RESOURCE_LIMIT).collect(Collectors.toList())
                                )
                        )
                ));
    }

    @Override
    public Map<Long, GamePlayCounts> getPlayCountsBatch(List<Long> gameIds, GameSortType sortType) {
        if (gameIds.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, GamePlayCounts> playCountsMap = new HashMap<>();

        // 총 플레이 횟수
        Map<Long, Integer> totalCountMap = getTotalPlayCounts(gameIds);

        // 주간 플레이 횟수 : WEEK, MONTH 일 때만 조회
        Map<Long, Integer> weekCountMap = new HashMap<>();
        if (sortType == GameSortType.WEEK || sortType == GameSortType.MONTH) {
            weekCountMap = getWeekPlayCounts(gameIds);
        }

        // 월간 플레이 횟수 : MONTH 일 때만 조회
        Map<Long, Integer> monthCountMap = new HashMap<>();
        if (sortType == GameSortType.MONTH) {
            monthCountMap = getMonthPlayCounts(gameIds);
        }

        for (Long gameId : gameIds) {
            int totalCount = totalCountMap.getOrDefault(gameId, 0);
            int weekCount = weekCountMap.getOrDefault(gameId, 0);
            int monthCount = monthCountMap.getOrDefault(gameId, 0);
            playCountsMap.put(gameId, new GamePlayCounts(totalCount, weekCount, monthCount));
        }

        return playCountsMap;
    }

    @Override
    public Map<Long, Integer> getTotalPlayCountsBatch(List<Long> gameIds) {
        return getTotalPlayCounts(gameIds);
    }

    @Override
    public GameBatchData getAllBatchData(List<Long> gameIds, GameSortType sortType) {
        return GameBatchData.of(
                getCategoriesBatch(gameIds),
                getTopResourcesBatch(gameIds),
                getPlayCountsBatch(gameIds, sortType)
        );
    }

    @Override
    public GameBatchData getTotalPlayBatchData(List<Long> gameIds) {
        return GameBatchData.ofTotalPlays(
                getCategoriesBatch(gameIds),
                getTopResourcesBatch(gameIds),
                getTotalPlayCountsBatch(gameIds)
        );
    }

    // =========================== 페이징 메서드들 ===========================

    @Override
    public <T> List<T> applyCursorPagingWithCustomCursor(List<T> sortedResponses, Long cursorId,
                                                         Function<T, Long> cursorExtractor, Pageable pageable) {
        if (cursorId == null) {
            return sortedResponses.stream()
                    .limit(pageable.getPageSize() + 1)
                    .collect(Collectors.toList());
        }

        int cursorIndex = findCursorIndex(sortedResponses, cursorId, cursorExtractor);
        if (cursorIndex == -1) {
            log.warn("Cursor not found: {}", cursorId);
            return Collections.emptyList();
        }

        return sortedResponses.stream()
                .skip(cursorIndex + 1)
                .limit(pageable.getPageSize() + 1)
                .collect(Collectors.toList());
    }

    @Override
    public <T extends CursorIdentifiable> List<T> applyCursorPaging(List<T> sortedResponses, Long cursorId, Pageable pageable) {
        return applyCursorPagingWithCustomCursor(sortedResponses, cursorId, T::getCursorValue, pageable);
    }

    // =========================== 응답 생성 메서드들 ===========================

    @Override
    public GameListSelectionResponse buildSelectionResponse(Tuple tuple) {
        return GameListSelectionResponse.builder()
                .id(tuple.get(GameQClasses.resources.id))
                .title(tuple.get(GameQClasses.resources.title))
                .type(tuple.get(GameQClasses.images.mediaType.coalesce(GameQClasses.links.mediaType)))
                .content(tuple.get(GameQClasses.images.fileUrl.coalesce(GameQClasses.links.urls)))
                .startSec(Optional.ofNullable(tuple.get(GameQClasses.links.startSec.coalesce(GameConstants.DEFAULT_SEC))).orElse(GameConstants.DEFAULT_SEC))
                .endSec(Optional.ofNullable(tuple.get(GameQClasses.links.endSec.coalesce(GameConstants.DEFAULT_SEC))).orElse(GameConstants.DEFAULT_SEC))
                .build();
    }

    @Override
    public GameListResponse buildGameListResponse(Tuple tuple, Users currentUser, GameBatchData batchData) {
        Long roomId = tuple.get(0, Long.class);
        if (roomId == null) {
            return null;
        }

        String nickname = tuple.get(3, String.class);
        String profileImageUrl = tuple.get(4, String.class);
        boolean isPrivate = Boolean.TRUE.equals(tuple.get(5, Boolean.class));

        if (isPrivate) {
            nickname = GameConstants.ANONYMOUS_NICKNAME;
            profileImageUrl = null;
        }

        List<Category> categories = batchData.getCategoriesMap().getOrDefault(roomId, Collections.emptyList());
        List<GameListSelectionResponse> selections = batchData.getSelectionsMap().getOrDefault(roomId, Collections.emptyList());

        // PlayCounts가 있으면 사용, 없으면 TotalPlayCounts 사용
        GamePlayCounts counts = batchData.getPlayCountsMap() != null ?
                batchData.getPlayCountsMap().getOrDefault(roomId, new GamePlayCounts(0, 0, 0)) :
                new GamePlayCounts(batchData.getTotalPlayCountsMap().getOrDefault(roomId, 0), 0, 0);

        // existsMine 계산
        Boolean existsMineResult = getExistsMineFromTuple(tuple, currentUser);
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

    // =========================== 유틸리티 메서드들 ===========================

    @Override
    public int safeIntValue(Long value) {
        return value != null ? Math.toIntExact(value) : 0;
    }

    @Override
    public boolean isGameAccessibleByUser(Long gameId, Users user) {
        if (gameId == null) {
            return false;
        }

        try {
            BooleanExpression accessCondition = user != null ?
                    GameQClasses.games.accessType.ne(AccessType.PRIVATE).or(GameQClasses.games.users.uid.eq(user.getUid())) :
                    GameQClasses.games.accessType.ne(AccessType.PRIVATE);

            return jpaQueryFactory
                    .selectOne()
                    .from(GameQClasses.games)
                    .where(GameQClasses.games.id.eq(gameId).and(accessCondition))
                    .fetchFirst() != null;
        } catch (Exception e) {
            log.error("Error checking game accessibility for gameId: {}, user: {}", gameId, user, e);
            return false;
        }
    }

    @Override
    public List<Long> extractGameIds(List<Tuple> tuples) {
        return tuples.stream()
                .map(tuple -> tuple.get(GameQClasses.games.id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    // =========================== 헬퍼 메서드들 ===========================

    private Map<Long, Integer> getTotalPlayCounts(List<Long> gameIds) {
        List<Tuple> totalResults = jpaQueryFactory
                .select(GameQClasses.games.id, GameQClasses.results.count())
                .from(GameQClasses.games)
                .leftJoin(GameQClasses.games.gameResources, GameQClasses.resources)
                .leftJoin(GameQClasses.results).on(GameQClasses.results.gameResources.eq(GameQClasses.resources))
                .where(GameQClasses.games.id.in(gameIds)
                        .and(GameQClasses.games.accessType.eq(AccessType.PUBLIC)))
                .groupBy(GameQClasses.games.id)
                .having(GameQClasses.resources.count().goe(GameConstants.MIN_RESOURCE_COUNT))
                .fetch();

        return totalResults.stream()
                .filter(tuple -> tuple.get(GameQClasses.games.id) != null)
                .collect(Collectors.toMap(
                        tuple -> tuple.get(GameQClasses.games.id),
                        tuple -> safeIntValue(tuple.get(1, Long.class))
                ));
    }

    private Map<Long, Integer> getWeekPlayCounts(List<Long> gameIds) {
        OffsetDateTime oneWeekAgo = OffsetDateTime.now().minusWeeks(1);

        List<Tuple> weekResults = jpaQueryFactory
                .select(GameQClasses.games.id, GameQClasses.results.count())
                .from(GameQClasses.games)
                .leftJoin(GameQClasses.games.gameResources, GameQClasses.resources)
                .leftJoin(GameQClasses.results).on(GameQClasses.results.gameResources.eq(GameQClasses.resources)
                        .and(GameQClasses.results.createdDate.after(oneWeekAgo)))
                .where(GameQClasses.games.id.in(gameIds)
                        .and(GameQClasses.games.accessType.eq(AccessType.PUBLIC)))
                .groupBy(GameQClasses.games.id)
                .having(GameQClasses.resources.count().goe(GameConstants.MIN_RESOURCE_COUNT))
                .fetch();

        return weekResults.stream()
                .filter(tuple -> tuple.get(GameQClasses.games.id) != null)
                .collect(Collectors.toMap(
                        tuple -> tuple.get(GameQClasses.games.id),
                        tuple -> safeIntValue(tuple.get(1, Long.class))
                ));
    }

    private Map<Long, Integer> getMonthPlayCounts(List<Long> gameIds) {
        OffsetDateTime oneMonthAgo = OffsetDateTime.now().minusMonths(1);

        List<Tuple> monthResults = jpaQueryFactory
                .select(GameQClasses.games.id, GameQClasses.results.count())
                .from(GameQClasses.games)
                .leftJoin(GameQClasses.games.gameResources, GameQClasses.resources)
                .leftJoin(GameQClasses.results).on(GameQClasses.results.gameResources.eq(GameQClasses.resources)
                        .and(GameQClasses.results.createdDate.after(oneMonthAgo)))
                .where(GameQClasses.games.id.in(gameIds)
                        .and(GameQClasses.games.accessType.eq(AccessType.PUBLIC)))
                .groupBy(GameQClasses.games.id)
                .having(GameQClasses.resources.count().goe(GameConstants.MIN_RESOURCE_COUNT))
                .fetch();

        return monthResults.stream()
                .filter(tuple -> tuple.get(GameQClasses.games.id) != null)
                .collect(Collectors.toMap(
                        tuple -> tuple.get(GameQClasses.games.id),
                        tuple -> safeIntValue(tuple.get(1, Long.class))
                ));
    }

    private <T> int findCursorIndex(List<T> items, Long cursorId, Function<T, Long> cursorExtractor) {
        for (int i = 0; i < items.size(); i++) {
            if (Objects.equals(cursorExtractor.apply(items.get(i)), cursorId)) {
                return i;
            }
        }
        return -1;
    }

    private Boolean getExistsMineFromTuple(Tuple tuple, Users currentUser) {
        if (currentUser == null) {
            return false;
        }

        try {
            return tuple.get(8, Boolean.class);
        } catch (Exception e) {
            // 인덱스가 맞지 않으면 직접 계산
            try {
                String gameOwnerUid = tuple.get(GameQClasses.games.users.uid);
                return currentUser.getUid().equals(gameOwnerUid);
            } catch (Exception ex) {
                log.warn("Could not determine existsMine from tuple", ex);
                return false;
            }
        }
    }
}
