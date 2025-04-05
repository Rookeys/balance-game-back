package com.games.balancegameback.service.game.impl;

import com.games.balancegameback.core.utils.CustomBasedPageImpl;
import com.games.balancegameback.core.utils.CustomPageImpl;
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
import com.games.balancegameback.service.game.repository.GameResultRepository;
import com.games.balancegameback.service.media.impl.S3Service;
import com.games.balancegameback.service.media.repository.ImageRepository;
import com.games.balancegameback.service.media.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameResourceService {

    private final S3Service s3Service;
    private final GameResourceRepository gameResourceRepository;
    private final GameResultRepository gameResultRepository;
    private final ImageRepository imageRepository;
    private final LinkRepository linkRepository;

    public Integer getCountResourcesInGames(Long gameId) {
        return gameResourceRepository.countByGameId(gameId);
    }

    public GameResourceResponse getResource(Long gameId, Long resourceId) {
        GameResources resources = gameResourceRepository.findById(resourceId);
        int totalNums = gameResultRepository.countByGameId(gameId);

        return GameResourceResponse.builder()
                .resourceId(resourceId)
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
                .totalPlayNums(totalNums)
                .winningNums(resources.getWinningLists().size())
                .build();
    }

    public CustomPageImpl<GameResourceResponse> getResources(Long gameId, Long cursorId, Pageable pageable,
                                                             GameResourceSearchRequest request) {
        return gameResourceRepository.findByGameId(gameId, cursorId, pageable, request);
    }

    public CustomBasedPageImpl<GameResourceResponse> getResourcesUsingPage(Long gameId, Pageable pageable,
                                                                           GameResourceSearchRequest request) {
        return gameResourceRepository.findByGameIdWithPaging(gameId, pageable, request);
    }

    @Transactional
    public void updateResource(Long resourceId, GameResourceRequest gameResourceRequest) {
        GameResources gameResources = gameResourceRepository.findById(resourceId);

        if (gameResourceRequest.getType().equals(MediaType.IMAGE) && gameResourceRequest.getContent() != null
                && gameResources.getImages() != null) {
            Images images = gameResources.getImages();
            images.update(gameResourceRequest.getContent());
            imageRepository.update(images);

            gameResources.update(gameResourceRequest.getTitle());
            gameResourceRepository.update(gameResources);

            if (!gameResources.getImages().getFileUrl().equals(images.getFileUrl())) {
                s3Service.deleteImageByUrl(gameResources.getImages().getFileUrl());
            }
        }

        if (gameResourceRequest.getType().equals(MediaType.LINK) && gameResourceRequest.getContent() != null
                && gameResources.getLinks() != null) {
            Links links = gameResources.getLinks();
            links.update(gameResourceRequest.getContent(), gameResourceRequest.getStartSec(),
                    gameResourceRequest.getEndSec());
            linkRepository.update(links);

            gameResources.update(gameResourceRequest.getTitle());
            gameResourceRepository.update(gameResources);
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
        GameResources resources = gameResourceRepository.findById(resourceId);
        gameResourceRepository.deleteById(resourceId);

        if (resources.getImages() != null) {
            s3Service.deleteImageByUrl(resources.getImages().getFileUrl());
        }
    }

    @Transactional
    public void deleteSelectResources(List<Long> list) {
        for (Long resourceId : list) {
            GameResources resources = gameResourceRepository.findById(resourceId);
            gameResourceRepository.deleteById(resourceId);

            if (resources.getImages() != null) {
                s3Service.deleteImageByUrl(resources.getImages().getFileUrl());
            }
        }
    }
}
