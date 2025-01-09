package com.games.balancegameback.service.media.impl;

import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.media.Images;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.media.ImageRequest;
import com.games.balancegameback.service.game.GameService;
import com.games.balancegameback.service.game.repository.GameRepository;
import com.games.balancegameback.service.media.repository.ImageRepository;
import com.games.balancegameback.service.user.impl.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final ImageRepository imageRepository;
    private final GameRepository gameRepository;
    private final GameService gameService;
    private final UserUtils userUtils;

    public void saveImageForGame(Long roomId, ImageRequest imageRequest) {
        Games games = gameRepository.findByRoomId(roomId);

        for (String fileUrl : imageRequest.getUrls()) {
            Images images = Images.builder()
                    .games(games)
                    .fileUrl(fileUrl)
                    .build();

            images = imageRepository.save(images);
            gameService.saveImageResource(games, images);
        }
    }

    public void saveImageForUser(ImageRequest imageRequest, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        Images images = Images.builder()
                .users(users)
                .fileUrl(imageRequest.getUrls().getFirst())
                .build();

        imageRepository.save(images);
    }

    public void updateImage(Long id, ImageRequest imageRequest) {
        Images images = imageRepository.findById(id);
        images.update(imageRequest.getUrls().getFirst());

        imageRepository.save(images);
    }

    public void deleteImage(Long id) {
        imageRepository.delete(id);
    }
}
