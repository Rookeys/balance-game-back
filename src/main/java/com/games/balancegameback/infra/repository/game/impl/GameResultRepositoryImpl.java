package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.domain.game.GameResults;
import com.games.balancegameback.domain.media.enums.MediaType;
import com.games.balancegameback.dto.game.GameResultResponse;
import com.games.balancegameback.infra.entity.*;
import com.games.balancegameback.infra.repository.game.GameResultJpaRepository;
import com.games.balancegameback.service.game.repository.GameResultRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class GameResultRepositoryImpl implements GameResultRepository {

    private final GameResultJpaRepository gameResultJpaRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<GameResultResponse> findGameResultRanking(Long gameId, Long cursorId, String searchQuery, Pageable pageable) {
        QGameResultsEntity gameResults = QGameResultsEntity.gameResultsEntity;
        QGameResourcesEntity gameResources = QGameResourcesEntity.gameResourcesEntity;
        QImagesEntity images = QImagesEntity.imagesEntity;
        QLinksEntity links = QLinksEntity.linksEntity;

        List<GameResultResponse> list = jpaQueryFactory
                .select(Projections.constructor(
                        GameResultResponse.class,
                        gameResources.id.as("resourceId"),
                        gameResources.title,
                        images.mediaType.coalesce(MediaType.LINK).as("type"),  // 기본값 Images
                        images.fileUrl.coalesce(links.urls).as("content"), // 이미지가 있으면 fileUrl, 없으면 링크 URL
                        links.startSec,
                        links.endSec,
                        this.calculateWinRate(gameId).as("winRate")
                ))
                .from(gameResults)
                .join(gameResults.gameResources, gameResources)
                .leftJoin(gameResources.images, images)
                .leftJoin(gameResources.links, links)
                .where(gameResources.games.id.eq(gameId),
                        cursorId != null ? gameResources.id.gt(cursorId) : null,
                        gameResources.title.contains(searchQuery))
                .orderBy(this.calculateWinRate(gameId).desc(), gameResources.id.asc()) // winRate 높은 순 정렬
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = false;
        if (list.size() > pageable.getPageSize()) {
            list.removeLast();
            hasNext = true;
        }

        return new PageImpl<>(list, pageable, hasNext ? pageable.getPageSize() + 1 : list.size());
    }

    @Override
    public int countByGameId(Long roomId) {
        return gameResultJpaRepository.countByGameResourcesGamesId(roomId);
    }

    @Override
    public int countByGameResourcesId(Long resourcesId) {
        return gameResultJpaRepository.countByGameResourcesId(resourcesId);
    }

    @Override
    public void save(GameResults gameResults) {
        gameResultJpaRepository.save(GameResultsEntity.from(gameResults));
    }

    private NumberExpression<Double> calculateWinRate(Long gameId) {
        QGameResultsEntity gameResults = QGameResultsEntity.gameResultsEntity;

        Long totalGames = jpaQueryFactory
                .select(gameResults.id.count())
                .from(gameResults)
                .where(gameResults.gameResources.games.id.eq(gameId))
                .fetchOne();

        if (totalGames == null || totalGames == 0) {
            totalGames = 1L; // 0으로 나누는 것을 방지하기 위해 최소 1 설정
        }

        return Expressions.numberTemplate(
                Double.class, "({0} / {1}) * 100", gameResults.id.count(), totalGames.doubleValue()
        );
    }
}
