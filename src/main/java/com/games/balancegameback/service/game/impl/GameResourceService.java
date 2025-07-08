package com.games.balancegameback.service.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.BadRequestException;
import com.games.balancegameback.core.exception.impl.NotFoundException;
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
import com.games.balancegameback.dto.media.AutoLinkRequest;
import com.games.balancegameback.dto.media.ImageRequest;
import com.games.balancegameback.dto.media.LinkRequest;
import com.games.balancegameback.service.game.repository.GameResourceRepository;
import com.games.balancegameback.service.game.repository.GameResultRepository;
import com.games.balancegameback.service.media.impl.S3Service;
import com.games.balancegameback.service.media.repository.ImageRepository;
import com.games.balancegameback.service.media.repository.LinkRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class GameResourceService {

    private final S3Service s3Service;
    private final GameRoomService gameRoomService;
    private final GameResourceRepository gameResourceRepository;
    private final GameResultRepository gameResultRepository;
    private final ImageRepository imageRepository;
    private final LinkRepository linkRepository;

    public Integer getCountResourcesInGames(Long gameId) {
        return gameResourceRepository.countByGameId(gameId);
    }

    public GameResourceResponse getResource(Long gameId, Long resourceId) {
        if (!gameResourceRepository.existsByGameIdAndResourceId(gameId, resourceId)) {
            throw new NotFoundException("잘못된 경로입니다.", ErrorCode.NOT_FOUND_EXCEPTION);
        }

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
        Integer beforeCount = gameResourceRepository.countByGameId(games.getId());

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

        Integer afterCount = gameResourceRepository.countByGameId(games.getId());
        checkResourceCountChangeAndRevalidate(games.getId(), beforeCount, afterCount);
    }

    @Transactional
    public void saveAutoLinkResource(Games games, List<AutoLinkRequest> autoLinkRequest) {
        Integer beforeCount = gameResourceRepository.countByGameId(games.getId());

        for (AutoLinkRequest request : autoLinkRequest) {
            Links links = Links.builder()
                    .users(null)
                    .games(games)
                    .mediaType(MediaType.LINK)
                    .urls(request.getUrl())
                    .startSec(request.getStartSec())
                    .endSec(request.getEndSec())
                    .build();

            GameResources gameResources = GameResources.builder()
                    .title(request.getTitle())
                    .links(links)
                    .games(games)
                    .build();

            gameResourceRepository.save(gameResources); // 한 번에 저장
        }

        Integer afterCount = gameResourceRepository.countByGameId(games.getId());
        checkResourceCountChangeAndRevalidate(games.getId(), beforeCount, afterCount);
    }

    @Transactional
    public void saveImageResource(Games games, ImageRequest imageRequest) {
        Integer beforeCount = gameResourceRepository.countByGameId(games.getId());

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

        // 이미지 리소스들 저장 후 게임 리소스 수 확인하여 revalidate 호출
        Integer afterCount = gameResourceRepository.countByGameId(games.getId());
        checkResourceCountChangeAndRevalidate(games.getId(), beforeCount, afterCount);
    }

    @Transactional
    public void deleteResource(Long resourceId) {
        GameResources resources = gameResourceRepository.findById(resourceId);
        Long gameId = resources.getGames().getId();

        // 삭제 전 리소스 수 확인
        Integer beforeCount = gameResourceRepository.countByGameId(gameId);

        gameResourceRepository.deleteById(resourceId);

        if (resources.getImages() != null) {
            s3Service.deleteImageByUrl(resources.getImages().getFileUrl());
        }

        // 삭제 후 리소스 수 확인하여 revalidate 호출
        Integer afterCount = gameResourceRepository.countByGameId(gameId);
        checkResourceCountChangeAndRevalidate(gameId, beforeCount, afterCount);
    }

    @Transactional
    public void deleteSelectResources(List<Long> list) {
        if (list == null || list.isEmpty()) {
            return;
        }

        // 첫 번째 리소스로 게임 ID 확인 (모든 리소스가 같은 게임에 속한다고 가정)
        GameResources firstResource = gameResourceRepository.findById(list.get(0));
        Long gameId = firstResource.getGames().getId();

        // 삭제 전 리소스 수 확인
        Integer beforeCount = gameResourceRepository.countByGameId(gameId);

        for (Long resourceId : list) {
            GameResources resources = gameResourceRepository.findById(resourceId);
            gameResourceRepository.deleteById(resourceId);

            if (resources.getImages() != null) {
                s3Service.deleteImageByUrl(resources.getImages().getFileUrl());
            }
        }

        // 삭제 후 리소스 수 확인하여 revalidate 호출
        Integer afterCount = gameResourceRepository.countByGameId(gameId);
        checkResourceCountChangeAndRevalidate(gameId, beforeCount, afterCount);
    }

    /**
     * 리소스 수 변화를 확인하고 2개 기준으로 상태가 변했을 때 revalidate를 호출하는 메서드
     *
     * @param gameId 게임 ID
     * @param beforeCount 변경 전 리소스 수
     * @param afterCount 변경 후 리소스 수
     */
    private void checkResourceCountChangeAndRevalidate(Long gameId, Integer beforeCount, Integer afterCount) {
        boolean wasPlayable = beforeCount >= 2;
        boolean isPlayable = afterCount >= 2;

        // 상태가 변했을 때만 revalidate 호출
        if (wasPlayable != isPlayable) {
            gameRoomService.revalidate("/game/" + gameId);
            log.info("Game {} resource count changed from {} to {}. Playable status changed from {} to {}. Revalidate called.",
                    gameId, beforeCount, afterCount, wasPlayable, isPlayable);
        }
    }
}
