package com.games.balancegameback.service.game.impl;

import com.games.balancegameback.domain.game.GameResources;
import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.media.Images;
import com.games.balancegameback.domain.media.Links;
import com.games.balancegameback.dto.game.GameResourceRequest;
import com.games.balancegameback.dto.game.GameResourceResponse;
import com.games.balancegameback.service.game.repository.GameResourceRepository;
import com.games.balancegameback.service.media.repository.ImageRepository;
import com.games.balancegameback.service.media.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GameResourceService {

    private final GameResourceRepository gameResourceRepository;
    private final ImageRepository imageRepository;
    private final LinkRepository linkRepository;

    public Page<GameResourceResponse> getResources(Pageable pageable, Long roomId, Long cursorId) {
        return gameResourceRepository.findByRoomId(roomId, cursorId, pageable);
    }

    @Transactional
    public void updateResource(GameResourceRequest gameResourceRequest) {
        GameResources gameResources = gameResourceRepository.findById(gameResourceRequest.getResourceId());

        if (gameResourceRequest.getFileUrl() != null) {
            Images images = gameResources.getImages();
            images.update(gameResourceRequest.getFileUrl());
            imageRepository.save(images);

            gameResources.updateImage(gameResourceRequest.getTitle(), images);
            gameResourceRepository.save(gameResources);

            // 연관 관계가 전부 끊긴 사진을 정리하는 트리거 추가 예정
        }

        if (gameResourceRequest.getLink() != null) {
            Links links = gameResources.getLinks();
            links.update(gameResourceRequest.getLink(), gameResourceRequest.getStartSec(),
                    gameResourceRequest.getEndSec());
            linkRepository.save(links);

            gameResources.updateLinks(gameResourceRequest.getTitle(), links);
            gameResourceRepository.save(gameResources);
        }
    }

    @Transactional
    public void saveLinkResource(Games games, Links links) {
        GameResources gameResources = GameResources.builder()
                .title(null)
                .links(links)
                .games(games)
                .build();

        gameResourceRepository.save(gameResources);
    }

    @Transactional
    public void saveImageResource(Games games, Images images) {
        GameResources gameResources = GameResources.builder()
                .title(null)
                .images(images)
                .games(games)
                .build();

        gameResourceRepository.save(gameResources);
    }

    @Transactional
    public void deleteResource(Long resourceId) {
        gameResourceRepository.deleteById(resourceId);
    }
}
