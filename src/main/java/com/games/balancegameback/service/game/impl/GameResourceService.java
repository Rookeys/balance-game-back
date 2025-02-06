package com.games.balancegameback.service.game.impl;

import com.games.balancegameback.domain.game.GameResources;
import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.media.Images;
import com.games.balancegameback.domain.media.Links;
import com.games.balancegameback.domain.media.enums.MediaType;
import com.games.balancegameback.dto.game.GameResourceRequest;
import com.games.balancegameback.dto.game.GameResourceResponse;
import com.games.balancegameback.dto.game.GameResourceSearchRequest;
import com.games.balancegameback.dto.media.ImageRequest;
import com.games.balancegameback.dto.media.LinkRequest;
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

    public Page<GameResourceResponse> getResources(Long gameId, Long cursorId, Pageable pageable,
                                                   GameResourceSearchRequest request) {
        return gameResourceRepository.findByGameId(gameId, cursorId, pageable, request);
    }

    @Transactional
    public void updateResource(Long resourceId, GameResourceRequest gameResourceRequest) {
        GameResources gameResources = gameResourceRepository.findById(resourceId);

        if (gameResourceRequest.getFileUrl() != null && gameResources.getImages() != null) {
            Images images = gameResources.getImages();
            images.update(gameResourceRequest.getFileUrl());
            imageRepository.update(images);

            gameResources.updateImage(gameResourceRequest.getTitle(), images);
            gameResourceRepository.save(gameResources);

            // 연관 관계가 전부 끊긴 사진을 정리하는 트리거 추가 예정
        }

        if (gameResourceRequest.getLink() != null && gameResources.getLinks() != null) {
            Links links = gameResources.getLinks();
            links.update(gameResourceRequest.getLink(), gameResourceRequest.getStartSec(),
                    gameResourceRequest.getEndSec());
            linkRepository.update(links);

            gameResources.updateLinks(gameResourceRequest.getTitle(), links);
            gameResourceRepository.save(gameResources);
        }
    }

    @Transactional
    public void saveLinkResource(Games games, LinkRequest linkRequest) {
        Links links = Links.builder()
                .users(null)
                .games(games)
                .mediaType(MediaType.LINK)
                .urls(linkRequest.getUrl())
                .startSec(linkRequest.getStartSec())
                .endSec(linkRequest.getEndSec())
                .build();

        GameResources gameResources = GameResources.builder()
                .title(null)
                .links(links)
                .games(games)
                .build();

        gameResourceRepository.save(gameResources); // 한 번에 저장
    }

    @Transactional
    public void saveImageResource(Games games, ImageRequest imageRequest) {
        for (String fileUrl : imageRequest.getUrls()) {
            Images images = Images.builder()
                    .games(games)
                    .fileUrl(fileUrl)
                    .mediaType(MediaType.IMAGE)
                    .build();

            GameResources gameResources = GameResources.builder()
                    .title(null)
                    .images(images)
                    .games(games)
                    .build();

            gameResourceRepository.save(gameResources); // 한 번에 저장
        }
    }

    @Transactional
    public void deleteResource(Long resourceId) {
        gameResourceRepository.deleteById(resourceId);
    }
}
