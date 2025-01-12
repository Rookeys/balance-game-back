package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.domain.game.GameResources;
import com.games.balancegameback.dto.game.GameResourceResponse;
import com.games.balancegameback.dto.game.GameResourceTemporaryResponse;
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

import java.util.ArrayList;
import java.util.List;

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
        return gameResourceJpaRepository.findById(id).get().toModel();
    }

    @Override
    public Page<GameResourceResponse> findByRoomId(Long roomId, Long cursorId, Pageable pageable) {
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
                .where(resources.games.id.eq(roomId),
                        cursorId != null ? resources.id.gt(cursorId) : null)
                .orderBy(resources.id.asc())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = false;
        if (list.size() > pageable.getPageSize()) {
            list.removeLast();
            hasNext = true;
        }

        int totalPlayNums = gameResultRepository.countByGameId(roomId);
        List<GameResourceResponse> responseList = new ArrayList<>();

        // 추후 Game Result 가 완성되고 통계 로직이 추가되면 교체 예정.
        for (GameResourceTemporaryResponse resource : list) {
            int winNums = gameResultRepository.countByGameResourcesId(resource.getResourceId());
            GameResourceResponse response = GameResourceResponse.builder()
                    .resourceId(resource.getResourceId())
                    .title(resource.getTitle())
                    .fileUrl(resource.getFileUrl())
                    .link(resource.getLink())
                    .startSec(resource.getStartSec())
                    .endSec(resource.getEndSec())
                    .build();

            if (totalPlayNums == 0) {
                response.update(0);
            } else {
                response.update((double) winNums / totalPlayNums * 100);
            }

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
}
