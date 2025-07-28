package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.utils.CustomBasedPageImpl;
import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.GameResults;
import com.games.balancegameback.domain.game.enums.GameResourceSortType;
import com.games.balancegameback.domain.media.enums.MediaType;
import com.games.balancegameback.dto.game.GameResourceSearchRequest;
import com.games.balancegameback.dto.game.GameResultResponse;
import com.games.balancegameback.infra.entity.*;
import com.games.balancegameback.infra.repository.game.GameResultJpaRepository;
import com.games.balancegameback.service.game.repository.GameResultRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class GameResultRepositoryImpl implements GameResultRepository {

    private final GameResultJpaRepository gameResultJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    // Q클래스 홀더
    private static class QEntities {
        static final QGameResultsEntity gameResults = QGameResultsEntity.gameResultsEntity;
        static final QGameResourcesEntity gameResources = QGameResourcesEntity.gameResourcesEntity;
        static final QImagesEntity images = QImagesEntity.imagesEntity;
        static final QLinksEntity links = QLinksEntity.linksEntity;
    }

    @Override
    public CustomPageImpl<GameResultResponse> findGameResultRanking(Long gameId, Long cursorId,
                                                                    GameResourceSearchRequest request,
                                                                    Pageable pageable) {
        try {
            Long totalElements = calculateTotalElements(gameId, request);

            List<GameResultResponse> allResponses = executeGameResultQuery(gameId, request);
            List<GameResultResponse> sortedResponses = applySorting(allResponses, request.getSortType());
            List<GameResultResponse> pagedResponses = applyCursorPaging(sortedResponses, cursorId, pageable);

            boolean hasNext = pagedResponses.size() > pageable.getPageSize();
            if (hasNext) {
                pagedResponses.removeLast();
            }

            return new CustomPageImpl<>(pagedResponses, pageable, totalElements, cursorId, hasNext);
        } catch (Exception e) {
            log.error("Error fetching game result ranking for gameId: {}", gameId, e);
            return new CustomPageImpl<>(Collections.emptyList(), pageable, 0L, cursorId, false);
        }
    }

    @Override
    public CustomBasedPageImpl<GameResultResponse> findGameResultRankingWithPaging(Long gameId, Pageable pageable,
                                                                                   GameResourceSearchRequest request) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QEntities.gameResources.games.id.eq(gameId));

        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            builder.and(QEntities.gameResources.title.containsIgnoreCase(request.getTitle()));
        }

        List<GameResultResponse> results = executeGameResultQueryWithPaging(gameId, request, pageable);

        Long totalCount = jpaQueryFactory
                .select(QEntities.gameResources.count())
                .from(QEntities.gameResources)
                .where(builder)
                .fetchOne();

        return new CustomBasedPageImpl<>(results, pageable, totalCount != null ? totalCount : 0L);
    }

    @Override
    public int countByGameId(Long roomId) {
        return Objects.requireNonNull(jpaQueryFactory
                .select(QEntities.gameResults.id.count())
                .from(QEntities.gameResults)
                .where(QEntities.gameResults.gameResources.games.id.eq(roomId))
                .fetchOne()).intValue();
    }

    @Override
    public int countByGameResourcesId(Long resourcesId) {
        return Objects.requireNonNull(jpaQueryFactory
                .select(QEntities.gameResults.id.count())
                .from(QEntities.gameResults)
                .where(QEntities.gameResults.gameResources.id.eq(resourcesId))
                .fetchOne()).intValue();
    }

    @Override
    public void save(GameResults gameResults) {
        gameResultJpaRepository.save(GameResultsEntity.from(gameResults));
    }

    // =========================== 메인 쿼리 실행 메서드 ===========================

    private List<GameResultResponse> executeGameResultQuery(Long gameId, GameResourceSearchRequest request) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QEntities.gameResources.games.id.eq(gameId));

        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            builder.and(QEntities.gameResources.title.containsIgnoreCase(request.getTitle()));
        }

        List<Tuple> tuples = jpaQueryFactory
                .select(QEntities.gameResources.id,                           // 0
                        QEntities.gameResources.title,                        // 1
                        QEntities.images.mediaType.coalesce(MediaType.LINK),  // 2
                        QEntities.images.fileUrl.coalesce(QEntities.links.urls), // 3
                        QEntities.links.startSec,                             // 4
                        QEntities.links.endSec)                               // 5
                .from(QEntities.gameResources)
                .leftJoin(QEntities.gameResources.images, QEntities.images)
                .leftJoin(QEntities.gameResources.links, QEntities.links)
                .where(builder)
                .fetch();

        if (tuples.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> resourceIds = tuples.stream()
                .map(tuple -> tuple.get(QEntities.gameResources.id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<Long, Integer> winningCountsMap = getWinningCountsBatch(resourceIds);
        int totalPlayNums = getTotalPlayNums(gameId);

        return tuples.stream()
                .map(tuple -> buildGameResultResponse(tuple, winningCountsMap, totalPlayNums))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<GameResultResponse> executeGameResultQueryWithPaging(Long gameId, GameResourceSearchRequest request, Pageable pageable) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QEntities.gameResources.games.id.eq(gameId));

        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            builder.and(QEntities.gameResources.title.containsIgnoreCase(request.getTitle()));
        }

        List<Tuple> tuples = jpaQueryFactory
                .select(QEntities.gameResources.id,
                        QEntities.gameResources.title,
                        QEntities.images.mediaType.coalesce(MediaType.LINK),
                        QEntities.images.fileUrl.coalesce(QEntities.links.urls),
                        QEntities.links.startSec,
                        QEntities.links.endSec)
                .from(QEntities.gameResources)
                .leftJoin(QEntities.gameResources.images, QEntities.images)
                .leftJoin(QEntities.gameResources.links, QEntities.links)
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        if (tuples.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> resourceIds = tuples.stream()
                .map(tuple -> tuple.get(QEntities.gameResources.id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<Long, Integer> winningCountsMap = getWinningCountsBatch(resourceIds);
        int totalPlayNums = getTotalPlayNums(gameId);

        List<GameResultResponse> responses = tuples.stream()
                .map(tuple -> buildGameResultResponse(tuple, winningCountsMap, totalPlayNums))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        return applySorting(responses, request.getSortType());
    }

    // =========================== 배치 조회 메서드 ===========================

    private Map<Long, Integer> getWinningCountsBatch(List<Long> resourceIds) {
        if (resourceIds.isEmpty()) {
            return Collections.emptyMap();
        }

        List<Tuple> winningCounts = jpaQueryFactory
                .select(QEntities.gameResources.id, QEntities.gameResources.winningLists.size())
                .from(QEntities.gameResources)
                .where(QEntities.gameResources.id.in(resourceIds))
                .fetch();

        return winningCounts.stream()
                .filter(tuple -> tuple.get(QEntities.gameResources.id) != null)
                .collect(Collectors.toMap(
                        tuple -> tuple.get(QEntities.gameResources.id),
                        tuple -> Optional.ofNullable(tuple.get(QEntities.gameResources.winningLists.size()))
                                .orElse(0)
                ));
    }

    // =========================== 정렬 및 페이징 메서드 ===========================

    private List<GameResultResponse> applySorting(List<GameResultResponse> responses, GameResourceSortType sortType) {
        return switch (sortType) {
            case OLD -> responses.stream()
                    .sorted(Comparator.comparing(GameResultResponse::getResourceId))
                    .collect(Collectors.toList());
            case RESENT -> responses.stream()
                    .sorted(Comparator.comparing(GameResultResponse::getResourceId).reversed())
                    .collect(Collectors.toList());
            case WIN_RATE_DESC -> responses.stream()
                    .sorted(Comparator.comparingInt(GameResultResponse::getWinningNums).reversed()
                            .thenComparing(Comparator.comparing(GameResultResponse::getResourceId).reversed()))
                    .collect(Collectors.toList());
            case WIN_RATE_ASC -> responses.stream()
                    .sorted(Comparator.comparingInt(GameResultResponse::getWinningNums)
                            .thenComparing(GameResultResponse::getResourceId))
                    .collect(Collectors.toList());
        };
    }

    private List<GameResultResponse> applyCursorPaging(List<GameResultResponse> sortedResponses, Long cursorId,
                                                       Pageable pageable) {
        if (cursorId == null) {
            return sortedResponses.stream()
                    .limit(pageable.getPageSize() + 1)
                    .collect(Collectors.toList());
        }

        int cursorIndex = -1;
        for (int i = 0; i < sortedResponses.size(); i++) {
            if (sortedResponses.get(i).getResourceId().equals(cursorId)) {
                cursorIndex = i;
                break;
            }
        }

        if (cursorIndex == -1) {
            log.warn("Cursor resource not found: {}", cursorId);
            return Collections.emptyList();
        }

        return sortedResponses.stream()
                .skip(cursorIndex + 1)
                .limit(pageable.getPageSize() + 1)
                .collect(Collectors.toList());
    }

    // =========================== 헬퍼 메서드 ===========================

    private GameResultResponse buildGameResultResponse(Tuple tuple, Map<Long, Integer> winningCountsMap, int totalPlayNums) {
        Long resourceId = tuple.get(QEntities.gameResources.id);
        if (resourceId == null) return null;

        return GameResultResponse.builder()
                .resourceId(resourceId)
                .title(tuple.get(QEntities.gameResources.title))
                .type(tuple.get(QEntities.images.mediaType.coalesce(MediaType.LINK)))
                .content(tuple.get(QEntities.images.fileUrl.coalesce(QEntities.links.urls)))
                .startSec(tuple.get(QEntities.links.startSec))
                .endSec(tuple.get(QEntities.links.endSec))
                .winningNums(winningCountsMap.getOrDefault(resourceId, 0))
                .totalPlayNums(totalPlayNums)
                .build();
    }

    private Long calculateTotalElements(Long gameId, GameResourceSearchRequest request) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QEntities.gameResources.games.id.eq(gameId));

        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            builder.and(QEntities.gameResources.title.containsIgnoreCase(request.getTitle()));
        }

        return jpaQueryFactory
                .select(QEntities.gameResources.count())
                .from(QEntities.gameResources)
                .where(builder)
                .fetchOne();
    }

    private int getTotalPlayNums(Long gameId) {
        Long count = jpaQueryFactory
                .select(QEntities.gameResults.count())
                .from(QEntities.gameResults)
                .where(QEntities.gameResults.gameResources.games.id.eq(gameId))
                .fetchOne();

        return count != null ? Math.toIntExact(count) : 0;
    }
}