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
import com.games.balancegameback.infra.repository.game.common.*;
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
    private final CommonGameRepository commonGameRepository;

    @Override
    public CustomPageImpl<GameResultResponse> findGameResultRanking(Long gameId, Long cursorId,
                                                                    GameResourceSearchRequest request,
                                                                    Pageable pageable) {
        try {
            List<GameResultResponse> allResponses = buildAllGameResultResponses(gameId, request);
            List<GameResultResponse> sortedResponses = applySorting(allResponses, request.getSortType());

            List<GameResultResponse> pagedResponses = commonGameRepository.applyCursorPaging(sortedResponses, cursorId, pageable);

            boolean hasNext = pagedResponses.size() > pageable.getPageSize();
            if (hasNext) {
                pagedResponses.removeLast();
            }

            return new CustomPageImpl<>(pagedResponses, pageable, (long) allResponses.size(), cursorId, hasNext);
        } catch (Exception e) {
            log.error("Error fetching game result ranking for gameId: {}", gameId, e);
            return new CustomPageImpl<>(Collections.emptyList(), pageable, 0L, cursorId, false);
        }
    }

    @Override
    public CustomBasedPageImpl<GameResultResponse> findGameResultRankingWithPaging(Long gameId, Pageable pageable,
                                                                                   GameResourceSearchRequest request) {
        try {
            List<GameResultResponse> allResponses = buildAllGameResultResponses(gameId, request);
            List<GameResultResponse> sortedResponses = applySorting(allResponses, request.getSortType());

            // 오프셋 페이징
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), sortedResponses.size());
            List<GameResultResponse> pagedResults = sortedResponses.subList(start, end);

            return new CustomBasedPageImpl<>(pagedResults, pageable, sortedResponses.size());
        } catch (Exception e) {
            log.error("Error fetching game result ranking with paging for gameId: {}", gameId, e);
            return new CustomBasedPageImpl<>(Collections.emptyList(), pageable, 0L);
        }
    }

    @Override
    public int countByGameId(Long roomId) {
        return commonGameRepository.safeIntValue(jpaQueryFactory
                .select(GameQClasses.results.id.count())
                .from(GameQClasses.results)
                .where(GameQClasses.results.gameResources.games.id.eq(roomId))
                .fetchOne());
    }

    @Override
    public int countByGameResourcesId(Long resourcesId) {
        return commonGameRepository.safeIntValue(jpaQueryFactory
                .select(GameQClasses.results.id.count())
                .from(GameQClasses.results)
                .where(GameQClasses.results.gameResources.id.eq(resourcesId))
                .fetchOne());
    }

    @Override
    public void save(GameResults gameResults) {
        gameResultJpaRepository.save(GameResultsEntity.from(gameResults));
    }

    // =========================== 간소화된 핵심 메서드들 ===========================

    private List<GameResultResponse> buildAllGameResultResponses(Long gameId, GameResourceSearchRequest request) {
        // 기본 데이터 조회
        List<Tuple> tuples = fetchGameResourceTuples(gameId, request);
        if (tuples.isEmpty()) {
            return Collections.emptyList();
        }

        // 승리 횟수 배치 조회
        List<Long> resourceIds = tuples.stream()
                .map(tuple -> tuple.get(GameQClasses.resources.id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        Map<Long, Integer> winningCountsMap = getWinningCountsBatch(resourceIds);
        int totalPlayNums = getTotalPlayNums(gameId);

        // 응답 생성
        return tuples.stream()
                .map(tuple -> buildGameResultResponse(tuple, winningCountsMap, totalPlayNums))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private List<Tuple> fetchGameResourceTuples(Long gameId, GameResourceSearchRequest request) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(GameQClasses.resources.games.id.eq(gameId));

        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            builder.and(GameQClasses.resources.title.containsIgnoreCase(request.getTitle()));
        }

        return jpaQueryFactory
                .select(GameQClasses.resources.id,
                        GameQClasses.resources.title,
                        GameQClasses.images.mediaType.coalesce(MediaType.LINK),
                        GameQClasses.images.fileUrl.coalesce(GameQClasses.links.urls),
                        GameQClasses.links.startSec,
                        GameQClasses.links.endSec)
                .from(GameQClasses.resources)
                .leftJoin(GameQClasses.resources.images, GameQClasses.images)
                .leftJoin(GameQClasses.resources.links, GameQClasses.links)
                .where(builder)
                .fetch();
    }

    private Map<Long, Integer> getWinningCountsBatch(List<Long> resourceIds) {
        if (resourceIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return jpaQueryFactory
                .select(GameQClasses.resources.id, GameQClasses.resources.winningLists.size())
                .from(GameQClasses.resources)
                .where(GameQClasses.resources.id.in(resourceIds))
                .fetch()
                .stream()
                .filter(tuple -> tuple.get(GameQClasses.resources.id) != null)
                .collect(Collectors.toMap(
                        tuple -> tuple.get(GameQClasses.resources.id),
                        tuple -> Optional.ofNullable(tuple.get(GameQClasses.resources.winningLists.size())).orElse(0)
                ));
    }

    private int getTotalPlayNums(Long gameId) {
        Long count = jpaQueryFactory
                .select(GameQClasses.results.count())
                .from(GameQClasses.results)
                .where(GameQClasses.results.gameResources.games.id.eq(gameId))
                .fetchOne();

        return commonGameRepository.safeIntValue(count);
    }

    private List<GameResultResponse> applySorting(List<GameResultResponse> responses, GameResourceSortType sortType) {
        Comparator<GameResultResponse> comparator = switch (sortType) {
            case OLD -> Comparator.comparing(GameResultResponse::getResourceId);
            case RESENT -> Comparator.comparing(GameResultResponse::getResourceId).reversed();
            case WIN_RATE_DESC -> Comparator
                    .comparingInt(GameResultResponse::getWinningNums).reversed()
                    .thenComparing(GameResultResponse::getResourceId);
            case WIN_RATE_ASC -> Comparator
                    .comparingInt(GameResultResponse::getWinningNums)
                    .thenComparing(GameResultResponse::getResourceId);
        };

        return responses.stream().sorted(comparator).collect(Collectors.toList());
    }

    private GameResultResponse buildGameResultResponse(Tuple tuple, Map<Long, Integer> winningCountsMap, int totalPlayNums) {
        Long resourceId = tuple.get(GameQClasses.resources.id);
        if (resourceId == null) return null;

        return GameResultResponse.builder()
                .resourceId(resourceId)
                .title(tuple.get(GameQClasses.resources.title))
                .type(tuple.get(GameQClasses.images.mediaType.coalesce(MediaType.LINK)))
                .content(tuple.get(GameQClasses.images.fileUrl.coalesce(GameQClasses.links.urls)))
                .startSec(tuple.get(GameQClasses.links.startSec))
                .endSec(tuple.get(GameQClasses.links.endSec))
                .winningNums(winningCountsMap.getOrDefault(resourceId, 0))
                .totalPlayNums(totalPlayNums)
                .build();
    }
}