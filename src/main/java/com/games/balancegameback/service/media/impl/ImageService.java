package com.games.balancegameback.service.media.impl;

import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.dto.media.ImageRequest;
import com.games.balancegameback.service.game.GameService;
import com.games.balancegameback.service.game.repository.GameRepository;
import com.games.balancegameback.service.media.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final GameRepository gameRepository;
    private final GameService gameService;

    @Transactional
    public void saveImage(Long gameId, ImageRequest imageRequest) {
        Games games = gameRepository.findByRoomId(gameId);
        gameService.saveImageResource(games, imageRequest);
    }

    @Transactional
    public void deleteImage(Long id) {
        imageRepository.delete(id);
    }
}
