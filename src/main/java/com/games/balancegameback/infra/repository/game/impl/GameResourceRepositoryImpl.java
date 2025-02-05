package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.domain.game.GameResources;
import com.games.balancegameback.dto.game.GameResourceResponse;
import com.games.balancegameback.dto.game.GameResourceTemporaryResponse;
import com.games.balancegameback.dto.game.gameplay.GamePlayResourceResponse;
import com.games.balancegameback.dto.game.gameplay.GamePlayResourceLinkResponse;
import com.games.balancegameback.infra.entity.*;
import com.games.balancegameback.infra.repository.game.GameResourceJpaRepository;
import com.games.balancegameback.service.game.repository.GameResourceRepository;
import com.games.balancegameback.service.game.repository.GameResultRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class GameResourceRepositoryImpl implements GameResourceRepository {

    private final GameResourceJpaRepository gameResourceJpaRepository;
    private final GameResultRepository gameResultRepository;
    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public void save(GameResources gameResources) {
        gameResourceJpaRepository.save(GameResourcesEntity.from(gameResources));
    }

    @Override
    public void update(GameResources gameResources) {
        Optional<GameResourcesEntity> entity = gameResourceJpaRepository.findById(gameResources.getId());
        entity.ifPresent(gameResourcesEntity -> gameResourcesEntity.update(gameResources));
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
    public Page<GameResourceResponse> findByGameId(Long gameId, Long cursorId, Pageable pageable) {
        QGameResourcesEntity resources = QGameResourcesEntity.gameResourcesEntity;
        QImagesEntity images = QImagesEntity.imagesEntity;
        QLinksEntity links = QLinksEntity.linksEntity;

        List<GameResourceTemporaryResponse> list = jpaQueryFactory
                .select(Projections.constructor(
                        GameResourceTemporaryResponse.class,
                        resources.id.as("resourceId"),
                        resources.title,
                        images.fileUrl.as("fileUrl"),
                        links.urls.as("link"),
                        links.startSec,
                        links.endSec
                ))
                .from(resources)
                .leftJoin(resources.images, images)
                .leftJoin(resources.links, links)
                .where(resources.games.id.eq(gameId),
                        cursorId != null ? resources.id.gt(cursorId) : null)
                .orderBy(resources.id.asc())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = false;
        if (list.size() > pageable.getPageSize()) {
            list.removeLast();
            hasNext = true;
        }

        int totalPlayNums = gameResultRepository.countByGameId(gameId);
        List<GameResourceResponse> responseList = new ArrayList<>();

        for (GameResourceTemporaryResponse resource : list) {
            Double winRate = this.calculateWinRate(totalPlayNums, resource.getResourceId());
            GameResourceResponse response = GameResourceResponse.builder()
                    .resourceId(resource.getResourceId())
                    .title(resource.getTitle())
                    .fileUrl(resource.getFileUrl())
                    .link(resource.getLink())
                    .startSec(resource.getStartSec())
                    .endSec(resource.getEndSec())
                    .winRate(winRate)
                    .build();

            responseList.add(response);
        }

        return new PageImpl<>(responseList, pageable, hasNext ? pageable.getPageSize() + 1 : list.size());
    }

    @Override
    public void deleteById(Long id) {
        if (!gameResourceJpaRepository.existsById(id)) {
            throw new NotFoundException("Not Found Resource!", ErrorCode.NOT_FOUND_EXCEPTION);
        }

        gameResourceJpaRepository.deleteById(id);
    }

    private Double calculateWinRate(int totalPlayNums, Long resourceId) {
        int winGames = gameResultRepository.countByGameResourcesId(resourceId);

        if (totalPlayNums == 0) {
            totalPlayNums = 1; // 0으로 나누는 것을 방지하기 위해 최소 1 설정
        }

        return (double) (((winGames / totalPlayNums)) * 100);
    }
}
