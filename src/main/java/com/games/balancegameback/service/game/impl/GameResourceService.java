package com.games.balancegameback.service.game.impl;

import com.games.balancegameback.domain.game.GameResources;
import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.media.Images;
import com.games.balancegameback.domain.media.Links;
import com.games.balancegameback.service.game.repository.GameResourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameResourceService {

    private final GameResourceRepository gameResourceRepository;

    public void saveLinkResource(Games games, Links links) {
        GameResources gameResources = GameResources.builder()
                .title(null)
                .links(links)
                .games(games)
                .build();

        gameResourceRepository.save(gameResources);
    }

    public void saveImageResource(Games games, Images images) {
        GameResources gameResources = GameResources.builder()
                .title(null)
                .images(images)
                .games(games)
                .build();

        gameResourceRepository.save(gameResources);
    }
}
