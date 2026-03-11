package com.games.balancegameback.service.media.impl;

import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.dto.media.ImageRequest;
import com.games.balancegameback.service.game.GameService;
import com.games.balancegameback.service.game.repository.GameRepository;
import com.games.balancegameback.service.media.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final GameRepository gameRepository;
    private final GameService gameService;

    @Transactional
    public void saveImage(Long gameId, ImageRequest imageRequest) {
        List<String> cleanUrls = imageRequest.getUrls().stream()
                .map(url -> url.contains("?") ? url.substring(0, url.indexOf("?")) : url)
                .collect(Collectors.toList());
        imageRequest.setUrls(cleanUrls);

        Games games = gameRepository.findByRoomId(gameId);
        gameService.saveImageResource(games, imageRequest);
    }

    @Transactional
    public void deleteImage(Long id) {
        imageRepository.delete(id);
    }
}
