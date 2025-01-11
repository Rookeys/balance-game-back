package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.domain.game.GameResources;
import com.games.balancegameback.dto.game.GameResourceResponse;
import com.games.balancegameback.infra.entity.*;
import com.games.balancegameback.infra.repository.game.GameResourceJpaRepository;
import com.games.balancegameback.service.game.repository.GameResourceRepository;
import com.games.balancegameback.service.game.repository.GameResultRepository;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

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
    public GameResources findById(Long id) {
        Optional<GameResourcesEntity> entity = gameResourceJpaRepository.findById(id);
        return entity.map(GameResourcesEntity::toModel).orElse(null);
    }

    @Override
    public Page<GameResourceResponse> findByRoomId(Long roomId, Long cursorId, Pageable pageable) {
        QGameResourcesEntity resources = QGameResourcesEntity.gameResourcesEntity;
        QImagesEntity images = QImagesEntity.imagesEntity;
        QLinksEntity links = QLinksEntity.linksEntity;

        List<GameResourceResponse> list = jpaQueryFactory
                .select(Projections.constructor(
                        GameResourceResponse.class,
                        resources.id.as("resourceId"),
                        resources.title,
                        images.fileUrl.as("fileUrl"),
                        links.urls.as("link"),
                        links.startSec,
                        links.endSec,
                        null
                ))
                .from(resources)
                .leftJoin(resources.images, images)
                .leftJoin(resources.links, links)
                .where(resources.games.id.eq(roomId),
                        cursorId != null ? resources.id.gt(cursorId) : null)
                .orderBy(resources.id.asc())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        int totalPlayNums = gameResultRepository.countByGameId(roomId);
        boolean hasNext = false;

        for (GameResourceResponse resource : list) {
            int winNums = gameResultRepository.countByGameResourcesId(resource.getResourceId());

            if (totalPlayNums == 0) {
                resource.update(0);
            } else {
                resource.update((double) winNums / totalPlayNums * 100);
            }
        }

        if (list.size() > pageable.getPageSize()) {
            list.removeLast();
            hasNext = true;
        }

        return new PageImpl<>(list, pageable, hasNext ? pageable.getPageSize() + 1 : list.size());
    }

    @Override
    public void deleteById(Long id) {
        gameResourceJpaRepository.deleteById(id);
    }
}
