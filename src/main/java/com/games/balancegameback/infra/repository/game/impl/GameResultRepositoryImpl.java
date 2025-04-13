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
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;

@Repository
@RequiredArgsConstructor
public class GameResultRepositoryImpl implements GameResultRepository {

    private final GameResultJpaRepository gameResultJpaRepository;
    private final GameResourceRepositoryImpl gameResourceRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public CustomPageImpl<GameResultResponse> findGameResultRanking(Long gameId, Long cursorId,
                                                          GameResourceSearchRequest request,
                                                          Pageable pageable) {
        QGameResultsEntity gameResults = QGameResultsEntity.gameResultsEntity;
        QGameResourcesEntity gameResources = QGameResourcesEntity.gameResourcesEntity;
        QImagesEntity images = QImagesEntity.imagesEntity;
        QLinksEntity links = QLinksEntity.linksEntity;

        // 동적 필터 적용
        BooleanBuilder builder = new BooleanBuilder();
        BooleanBuilder totalCountBuilder = new BooleanBuilder();

        builder.and(gameResources.games.id.eq(gameId));
        totalCountBuilder.and(gameResources.games.id.eq(gameId));

        this.setOptions(builder, totalCountBuilder, cursorId, request);
        // resource repository 로직 재사용
        OrderSpecifier<?> orderSpecifier = gameResourceRepository.getOrderSpecifier(request.getSortType());

        List<GameResultResponse> list = jpaQueryFactory
                .select(Projections.constructor(
                        GameResultResponse.class,
                        gameResources.id.as("resourceId"),
                        gameResources.title,
                        images.mediaType.coalesce(MediaType.LINK).as("type"),  // 기본값 Images
                        images.fileUrl.coalesce(links.urls).as("content"), // 이미지가 있으면 fileUrl, 없으면 링크 URL
                        links.startSec,
                        links.endSec,
                        gameResources.winningLists.size().as("winningLists"),
                        gameResourceRepository.getTotalPlayNumsSubQuery(gameId)
                ))
                .from(gameResources)
                .leftJoin(gameResults).on(gameResults.gameResources.eq(gameResources))
                .leftJoin(gameResources.images, images)
                .leftJoin(gameResources.links, links)
                .where(builder)
                .groupBy(gameResources.id)
                .orderBy(orderSpecifier)
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = list.size() > pageable.getPageSize();

        if (hasNext) {
            list.removeLast(); // 안전한 마지막 요소 제거
        }

        Long totalElements = jpaQueryFactory
                .select(gameResources.count())
                .from(gameResources)
                .where(totalCountBuilder)
                .fetchOne();

        return new CustomPageImpl<>(list, pageable, totalElements, cursorId, hasNext);
    }

    @Override
    public CustomBasedPageImpl<GameResultResponse> findGameResultRankingWithPaging(Long gameId, Pageable pageable,
                                                                                   GameResourceSearchRequest request) {
        QGameResourcesEntity gameResources = QGameResourcesEntity.gameResourcesEntity;
        QGameResultsEntity gameResults = QGameResultsEntity.gameResultsEntity;
        QImagesEntity images = QImagesEntity.imagesEntity;
        QLinksEntity links = QLinksEntity.linksEntity;

        BooleanBuilder builder = new BooleanBuilder();
        builder.and(gameResources.games.id.eq(gameId));

        // 검색어 필터
        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            builder.and(gameResources.title.containsIgnoreCase(request.getTitle()));
        }

        // 정렬 기준
        OrderSpecifier<?> orderSpecifier = gameResourceRepository.getOrderSpecifier(request.getSortType());

        // 본문 조회
        List<GameResultResponse> results = jpaQueryFactory
                .select(Projections.constructor(
                        GameResultResponse.class,
                        gameResources.id.as("resourceId"),
                        gameResources.title,
                        images.mediaType.coalesce(MediaType.LINK).as("type"),
                        images.fileUrl.coalesce(links.urls).as("content"),
                        links.startSec,
                        links.endSec,
                        gameResources.winningLists.size().as("winningLists"),
                        gameResourceRepository.getTotalPlayNumsSubQuery(gameId)
                ))
                .from(gameResources)
                .leftJoin(gameResults).on(gameResults.gameResources.eq(gameResources))
                .leftJoin(gameResources.images, images)
                .leftJoin(gameResources.links, links)
                .where(builder)
                .groupBy(gameResources.id)
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수
        Long totalCount = jpaQueryFactory
                .select(gameResources.count())
                .from(gameResources)
                .where(builder)
                .fetchOne();

        return new CustomBasedPageImpl<>(results, pageable, totalCount != null ? totalCount : 0L);
    }

    @Override
    public int countByGameId(Long roomId) {
        QGameResultsEntity gameResults = QGameResultsEntity.gameResultsEntity;

        return Objects.requireNonNull(jpaQueryFactory
                .select(gameResults.id.count())
                .from(gameResults)
                .where(gameResults.gameResources.games.id.eq(roomId))
                .fetchOne()).intValue();
    }

    @Override
    public int countByGameResourcesId(Long resourcesId) {
        QGameResultsEntity gameResults = QGameResultsEntity.gameResultsEntity;

        return Objects.requireNonNull(jpaQueryFactory
                .select(gameResults.id.count())
                .from(gameResults)
                .where(gameResults.gameResources.id.eq(resourcesId))
                .fetchOne()).intValue();
    }

    @Override
    public void save(GameResults gameResults) {
        gameResultJpaRepository.save(GameResultsEntity.from(gameResults));
    }

    private void setOptions(BooleanBuilder builder, BooleanBuilder totalCountBuilder, Long cursorId,
                            GameResourceSearchRequest request) {
        QGameResourcesEntity gameResources = QGameResourcesEntity.gameResourcesEntity;

        if (cursorId != null && request.getSortType().equals(GameResourceSortType.OLD)) {
            builder.and(gameResources.id.gt(cursorId));
        }

        if (cursorId != null && request.getSortType().equals(GameResourceSortType.RESENT)) {
            builder.and(gameResources.id.lt(cursorId));
        }

        if (request.getSortType().equals(GameResourceSortType.WIN_RATE_DESC) ||
                request.getSortType().equals(GameResourceSortType.WIN_RATE_ASC)) {    // resource repository 로직 재사용
            gameResourceRepository.applyOtherSortOptions(builder, cursorId, request, gameResources);
        }

        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            builder.and(gameResources.title.containsIgnoreCase(request.getTitle()));
            totalCountBuilder.and(gameResources.title.containsIgnoreCase(request.getTitle()));
        }
    }
}
