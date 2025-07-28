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
            List<GameResultResponse> allResponses = getAllGameResults(gameId, request);
            List<GameResultResponse> sortedResponses = applySorting(allResponses, request.getSortType());
            List<GameResultResponse> pagedResponses = applyCursorPaging(sortedResponses, cursorId, pageable);

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
            List<GameResultResponse> allResponses = getAllGameResults(gameId, request);
            List<GameResultResponse> sortedResponses = applySorting(allResponses, request.getSortType());

            // 오프셋 페이징
            int start = (int) pageable.getOffset();
            int end = Math.min(start + pageable.getPageSize(), sortedResponses.size());
            List<GameResultResponse> pagedResults = sortedResponses.subList(start, end);

            return new CustomBasedPageImpl<>(pagedResults, pageable, (long) sortedResponses.size());
        } catch (Exception e) {
            log.error("Error fetching game result ranking with paging for gameId: {}", gameId, e);
            return new CustomBasedPageImpl<>(Collections.emptyList(), pageable, 0L);
        }
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

    // =========================== 핵심 메서드들 ===========================

    /**
     * 게임 결과 전체 조회
     */
    private List<GameResultResponse> getAllGameResults(Long gameId, GameResourceSearchRequest request) {
        List<Tuple> tuples = fetchBasicGameResourceData(gameId, request);
        if (tuples.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> resourceIds = extractResourceIds(tuples);
        Map<Long, Integer> winningCountsMap = getWinningCountsBatch(resourceIds);
        int totalPlayNums = getTotalPlayNums(gameId);

        return tuples.stream()
                .map(tuple -> buildGameResultResponse(tuple, winningCountsMap, totalPlayNums))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 기본 게임 리소스 데이터 조회
     */
    private List<Tuple> fetchBasicGameResourceData(Long gameId, GameResourceSearchRequest request) {
        BooleanBuilder builder = createSearchCondition(gameId, request);

        return jpaQueryFactory
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
                .fetch();
    }

    /**
     * 검색 조건 생성
     */
    private BooleanBuilder createSearchCondition(Long gameId, GameResourceSearchRequest request) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(QEntities.gameResources.games.id.eq(gameId));

        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            builder.and(QEntities.gameResources.title.containsIgnoreCase(request.getTitle()));
        }

        return builder;
    }

    /**
     * Tuple에서 resourceId 추출
     */
    private List<Long> extractResourceIds(List<Tuple> tuples) {
        return tuples.stream()
                .map(tuple -> tuple.get(QEntities.gameResources.id))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * 승리 횟수 배치 조회
     */
    private Map<Long, Integer> getWinningCountsBatch(List<Long> resourceIds) {
        if (resourceIds.isEmpty()) {
            return Collections.emptyMap();
        }

        return jpaQueryFactory
                .select(QEntities.gameResources.id, QEntities.gameResources.winningLists.size())
                .from(QEntities.gameResources)
                .where(QEntities.gameResources.id.in(resourceIds))
                .fetch()
                .stream()
                .filter(tuple -> tuple.get(QEntities.gameResources.id) != null)
                .collect(Collectors.toMap(
                        tuple -> tuple.get(QEntities.gameResources.id),
                        tuple -> Optional.ofNullable(tuple.get(QEntities.gameResources.winningLists.size())).orElse(0)
                ));
    }

    /**
     * 전체 플레이 수 조회
     */
    private int getTotalPlayNums(Long gameId) {
        Long count = jpaQueryFactory
                .select(QEntities.gameResults.count())
                .from(QEntities.gameResults)
                .where(QEntities.gameResults.gameResources.games.id.eq(gameId))
                .fetchOne();

        return count != null ? Math.toIntExact(count) : 0;
    }

    /**
     * 정렬 적용
     */
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

        return responses.stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * 커서 페이징 적용
     */
    private List<GameResultResponse> applyCursorPaging(List<GameResultResponse> sortedResponses, Long cursorId, Pageable pageable) {
        if (cursorId == null) {
            return sortedResponses.stream()
                    .limit(pageable.getPageSize() + 1)
                    .collect(Collectors.toList());
        }

        int cursorIndex = findCursorIndex(sortedResponses, cursorId);
        if (cursorIndex == -1) {
            log.warn("Cursor resource not found: {}", cursorId);
            return Collections.emptyList();
        }

        return sortedResponses.stream()
                .skip(cursorIndex + 1)
                .limit(pageable.getPageSize() + 1)
                .collect(Collectors.toList());
    }

    /**
     * 커서 인덱스 찾기
     */
    private int findCursorIndex(List<GameResultResponse> responses, Long cursorId) {
        for (int i = 0; i < responses.size(); i++) {
            if (responses.get(i).getResourceId().equals(cursorId)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * 게임 결과 응답 객체 생성
     */
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
}