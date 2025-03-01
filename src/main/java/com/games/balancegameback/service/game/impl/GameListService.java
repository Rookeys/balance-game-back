package com.games.balancegameback.service.game.impl;

import com.games.balancegameback.service.game.repository.GameListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameListService {

    private final GameListRepository gameListRepository;


}
