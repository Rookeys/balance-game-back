package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.core.utils.PaginationUtils;
import com.games.balancegameback.domain.game.GameResources;
import com.games.balancegameback.domain.game.enums.SortType;
import com.games.balancegameback.dto.game.GameResourceResponse;
import com.games.balancegameback.dto.game.GameResourceSearchRequest;
import com.games.balancegameback.dto.game.gameplay.GamePlayResourceResponse;
import com.games.balancegameback.dto.game.gameplay.GamePlayResourceLinkResponse;
import com.games.balancegameback.infra.entity.*;
import com.games.balancegameback.infra.repository.game.GameResourceJpaRepository;
import com.games.balancegameback.service.game.repository.GameResourceRepository;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.*;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
                .map(resource -> {
                    GamePlayResourceLinkResponse gameResourceLink = GamePlayResourceLinkResponse.builder()
                            .link(resource.getLinks() == null ? null : resource.getLinks().getUrls())
                            .startSec(resource.getLinks() == null ? 0 : resource.getLinks().getStartSec())
                            .endSec(resource.getLinks() == null ? 0 : resource.getLinks().getEndSec())
                            .build();

                    return GamePlayResourceResponse.builder()
                            .resourceId(resource.getId())
                            .title(resource.getTitle())
                            .fileUrl(resource.getImages().getFileUrl())
                            .gameResourceLink(gameResourceLink)
                            .build();
                }).toList();
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
    public Page<GameResourceResponse> findByGameId(Long gameId, Long cursorId, Pageable pageable, GameResourceSearchRequest request) {
        QGameResourcesEntity resources = QGameResourcesEntity.gameResourcesEntity;
        QImagesEntity images = QImagesEntity.imagesEntity;
        QLinksEntity links = QLinksEntity.linksEntity;

        // 동적 필터 적용
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(resources.games.id.eq(gameId));

        if (cursorId != null) {
            builder.and(resources.id.gt(cursorId));
        }

        if (request.getTitle() != null && !request.getTitle().isEmpty()) {
            builder.and(resources.title.containsIgnoreCase(request.getTitle()));
        }

        // 동적 정렬 조건
        OrderSpecifier<?> orderSpecifier = getOrderSpecifier(request.getSortType(), this.getWinRateSubQuery(gameId));

        List<GameResourceResponse> list = jpaQueryFactory
                .select(Projections.constructor(
                        GameResourceResponse.class,
                        resources.id.as("resourceId"),
                        resources.title,
                        images.fileUrl.as("fileUrl"),
                        links.urls.as("link"),
                        links.startSec,
                        links.endSec,
                        this.getWinRateSubQuery(gameId)
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
                .where(builder)
                .fetchOne();

        return new CustomPageImpl<>(list, pageable, totalElements, cursorId, hasNext);
    }

    @Override
    public void deleteById(Long id) {
        if (!gameResourceJpaRepository.existsById(id)) {
            throw new NotFoundException("Not Found Resource!", ErrorCode.NOT_FOUND_EXCEPTION);
        }

        gameResourceJpaRepository.deleteById(id);
    }

    public OrderSpecifier<?> getOrderSpecifier(SortType sortType, JPQLQuery<Double> winRateSubQuery) {
        QGameResourcesEntity resources = QGameResourcesEntity.gameResourcesEntity;

        if (sortType == null) {
            return resources.id.asc();
        }

        return switch (sortType) {
            case winRateAsc -> new OrderSpecifier<>(Order.ASC, winRateSubQuery);
            case winRateDesc -> new OrderSpecifier<>(Order.DESC, winRateSubQuery);
            case idDesc -> resources.id.desc();
            default -> resources.id.asc();
        };
    }

    public JPQLQuery<Double> getWinRateSubQuery(Long gameId) {
        QGameResultsEntity gameResults = QGameResultsEntity.gameResultsEntity;
        QGameResourcesEntity resources = QGameResourcesEntity.gameResourcesEntity;

        return JPAExpressions
                .select(Expressions.numberTemplate(Double.class, "ROUND(({0} / NULLIF({1}, 0)) * 100, 2)",
                        gameResults.id.count().doubleValue(),
                        JPAExpressions.select(gameResults.id.count().doubleValue())
                                .from(gameResults)
                                .where(gameResults.gameResources.games.id.eq(gameId))))
                .from(gameResults)
                .where(gameResults.gameResources.id.eq(resources.id));
    }
}
