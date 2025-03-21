package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.core.utils.PaginationUtils;
import com.games.balancegameback.domain.game.GameResources;
import com.games.balancegameback.domain.game.enums.GameResourceSortType;
import com.games.balancegameback.domain.media.enums.MediaType;
import com.games.balancegameback.dto.game.GameResourceResponse;
import com.games.balancegameback.dto.game.GameResourceSearchRequest;
import com.games.balancegameback.dto.game.gameplay.GamePlayResourceResponse;
import com.games.balancegameback.infra.entity.*;
import com.games.balancegameback.infra.repository.game.GameResourceJpaRepository;
import com.games.balancegameback.service.game.repository.GameResourceRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GameResourceRepositoryImpl implements GameResourceRepository {

    private final GameResourceJpaRepository gameResourceJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public void save(GameResources gameResources) {
        gameResourceJpaRepository.save(GameResourcesEntity.from(gameResources));
    }

    @Override
    public GameResources findById(Long id) {
        Optional<GameResourcesEntity> entity = gameResourceJpaRepository.findById(id);

        if (entity.isEmpty()) {
            throw new NotFoundException("Not Found Data!!", ErrorCode.NOT_FOUND_EXCEPTION);
        }

        return entity.get().toModel();
    }

    @Override
    public List<GamePlayResourceResponse> findByIds(List<Long> ids) {
        QGameResourcesEntity gameResource = QGameResourcesEntity.gameResourcesEntity;

        List<GameResources> gameResourcesList = jpaQueryFactory
                .selectFrom(gameResource)
                .where(gameResource.id.in(ids))
                .fetch()
                .stream()
                .map(GameResourcesEntity::toModel)
                .toList();

        return gameResourcesList.stream()
                .map(resources -> GamePlayResourceResponse.builder()
                        .resourceId(resources.getId())
                        .title(resources.getTitle())
                        .type(resources.getImages() != null ?
                                resources.getImages().getMediaType() :
                                resources.getLinks().getMediaType())
                        .content(resources.getImages() != null ?
                                resources.getImages().getFileUrl() :
                                resources.getLinks().getUrls())
                        .startSec(resources.getLinks() != null ?
                                resources.getLinks().getStartSec() : 0)
                        .endSec(resources.getLinks() != null ?
                                resources.getLinks().getEndSec() : 0)
                        .build())
                .toList();
    }

    @Override
    public List<Long> findByRandomId(Long gameId, int roundNumber) {
        QGameResourcesEntity resources = QGameResourcesEntity.gameResourcesEntity;

        return jpaQueryFactory
                .select(resources.id)
                .from(resources)
                .where(resources.games.id.eq(gameId))
                .orderBy(Expressions.numberTemplate(Double.class, "function('RAND')").asc())
                .limit(roundNumber)
                .fetch();
    }

    @Override
    public CustomPageImpl<GameResourceResponse> findByGameId(Long gameId, Long cursorId, Pageable pageable, GameResourceSearchRequest request) {
        QGameResourcesEntity resources = QGameResourcesEntity.gameResourcesEntity;
        QImagesEntity images = QImagesEntity.imagesEntity;
        QLinksEntity links = QLinksEntity.linksEntity;

        // 동적 필터 적용
        BooleanBuilder builder = new BooleanBuilder();
        BooleanBuilder totalBuilder = new BooleanBuilder(); // totalElements 를 위해 사용 (cursorId 배제)

        builder.and(resources.games.id.eq(gameId));
        totalBuilder.and(resources.games.id.eq(gameId));

        this.setOptions(builder, totalBuilder, cursorId, request, resources);
        // 동적 정렬 조건
        OrderSpecifier<?> orderSpecifier = this.getOrderSpecifier(request.getSortType());

        List<GameResourceResponse> list = jpaQueryFactory
                .select(Projections.constructor(
                        GameResourceResponse.class,
                        resources.id.as("resourceId"),
                        resources.title,
                        images.mediaType.coalesce(MediaType.LINK).as("type"),
                        images.fileUrl.coalesce(links.urls).as("content"),
                        links.startSec,
                        links.endSec,
                        resources.winningLists.size().as("winningLists"),
                        this.getTotalPlayNumsSubQuery(gameId)
                ))
                .from(resources)
                .leftJoin(resources.images, images)
                .leftJoin(resources.links, links)
                .where(builder)
                .orderBy(orderSpecifier) // 동적 정렬
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = PaginationUtils.hasNextPage(list, pageable.getPageSize());
        PaginationUtils.removeLastIfHasNext(list, pageable.getPageSize());

        Long totalElements = jpaQueryFactory
                .select(resources.count())
                .from(resources)
                .where(totalBuilder)
                .fetchOne();

        return new CustomPageImpl<>(list, pageable, totalElements, cursorId, hasNext);
    }

    @Override
    public void update(GameResources gameResources) {
        GameResourcesEntity entity = gameResourceJpaRepository.findById(gameResources.getId())
                .orElseThrow(() -> new NotFoundException("해당하는 정보가 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

        entity.update(gameResources);
    }

    @Override
    public Integer countByGameId(Long gameId) {
        return gameResourceJpaRepository.countByGamesId(gameId);
    }

    @Override
    public void deleteById(Long id) {
        if (!gameResourceJpaRepository.existsById(id)) {
            throw new NotFoundException("Not Found Resource!", ErrorCode.NOT_FOUND_EXCEPTION);
        }

        gameResourceJpaRepository.deleteById(id);
    }

    private void setOptions(BooleanBuilder builder, BooleanBuilder totalBuilder, Long cursorId,
                            GameResourceSearchRequest request,
                            QGameResourcesEntity resources) {
        if (cursorId != null && request.getSortType().equals(GameResourceSortType.OLD)) {
            builder.and(resources.id.gt(cursorId));
        }

        if (cursorId != null && request.getSortType().equals(GameResourceSortType.RESENT)) {
            builder.and(resources.id.lt(cursorId));
        }

        if (request.getSortType().equals(GameResourceSortType.WIN_RATE_DESC) ||
                request.getSortType().equals(GameResourceSortType.WIN_RATE_ASC)) {
            this.applyOtherSortOptions(builder, cursorId, request, resources);
        }

        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            builder.and(resources.title.containsIgnoreCase(request.getTitle()));
            totalBuilder.and(resources.title.containsIgnoreCase(request.getTitle()));
        }
    }

    public OrderSpecifier<?> getOrderSpecifier(GameResourceSortType gameResourceSortType) {
        QGameResourcesEntity resources = QGameResourcesEntity.gameResourcesEntity;

        return switch (gameResourceSortType) {
            case WIN_RATE_ASC -> resources.winningLists.size().asc();
            case WIN_RATE_DESC -> resources.winningLists.size().desc();
            case RESENT -> resources.id.desc();
            default -> resources.id.asc();
        };
    }

    public JPQLQuery<Integer> getTotalPlayNumsSubQuery(Long gameId) {
        QGameResultsEntity gameResults = QGameResultsEntity.gameResultsEntity;

        return JPAExpressions
                .select(gameResults.id.count().intValue())
                .from(gameResults)
                .where(gameResults.gameResources.games.id.eq(gameId));
    }

    public void applyOtherSortOptions(BooleanBuilder builder, Long cursorId,
                                       GameResourceSearchRequest request, QGameResourcesEntity resources) {
        NumberExpression<Integer> winningNums = resources.winningLists.size().coalesce(0);
        Integer cursorWinningNums = null;

        if (cursorId != null) {
            cursorWinningNums = jpaQueryFactory
                    .select(resources.winningLists.size().coalesce(0))
                    .from(resources)
                    .where(resources.id.eq(cursorId))
                    .fetchOne();
        }

        // cursorId 가 없다면 바로 탈출
        if (cursorId == null || cursorWinningNums == null) {
            return;
        }

        if (request.getSortType().equals(GameResourceSortType.WIN_RATE_DESC)) {
            builder.and(
                    new BooleanBuilder()
                            .or(winningNums.lt(cursorWinningNums))
                            .or(winningNums.eq(cursorWinningNums).and(resources.id.gt(cursorId)))
            );
        }

        if (request.getSortType().equals(GameResourceSortType.WIN_RATE_ASC)) {
            builder.and(
                    new BooleanBuilder()
                            .or(winningNums.gt(cursorWinningNums))
                            .or(winningNums.eq(cursorWinningNums).and(resources.id.gt(cursorId)))
            );
        }
    }
}
