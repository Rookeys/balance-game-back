package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.core.utils.PaginationUtils;
import com.games.balancegameback.domain.game.GameResults;
import com.games.balancegameback.domain.game.enums.CommentSortType;
import com.games.balancegameback.domain.game.enums.GameResourceSortType;
import com.games.balancegameback.domain.media.enums.MediaType;
import com.games.balancegameback.dto.game.GameResourceSearchRequest;
import com.games.balancegameback.dto.game.GameResultResponse;
import com.games.balancegameback.dto.game.comment.GameCommentSearchRequest;
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
        // 추후 옵션이 추가되면 switch 문으로 변경 예정.
        OrderSpecifier<?> orderSpecifier = gameResourceRepository.getOrderSpecifier(request.getSortType(),
                                                gameResourceRepository.getWinRateSubQuery(gameId));

        List<GameResultResponse> list = jpaQueryFactory
                .select(Projections.constructor(
                        GameResultResponse.class,
                        gameResources.id.as("resourceId"),
                        gameResources.title,
                        images.mediaType.coalesce(MediaType.LINK).as("type"),  // 기본값 Images
                        images.fileUrl.coalesce(links.urls).as("content"), // 이미지가 있으면 fileUrl, 없으면 링크 URL
                        links.startSec,
                        links.endSec,
                        gameResourceRepository.getWinRateSubQuery(gameId)
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

        boolean hasNext = PaginationUtils.hasNextPage(list, pageable.getPageSize());
        PaginationUtils.removeLastIfHasNext(list, pageable.getPageSize());

        Long totalElements = jpaQueryFactory
                .select(gameResources.count())
                .from(gameResources)
                .where(totalCountBuilder)
                .fetchOne();

        return new CustomPageImpl<>(list, pageable, totalElements, cursorId, hasNext);
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

        if (cursorId != null && request.getSortType().equals(GameResourceSortType.idAsc)) {
            builder.and(gameResources.id.gt(cursorId));
        }

        if (cursorId != null && request.getSortType().equals(GameResourceSortType.idDesc)) {
            builder.and(gameResources.id.lt(cursorId));
        }

        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            builder.and(gameResources.title.containsIgnoreCase(request.getTitle()));
            totalCountBuilder.and(gameResources.title.containsIgnoreCase(request.getTitle()));
        }
    }
}
