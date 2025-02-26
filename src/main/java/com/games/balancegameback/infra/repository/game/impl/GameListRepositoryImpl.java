package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.service.game.repository.GameListRepository;
import com.games.balancegameback.service.game.repository.GameRepository;
import com.games.balancegameback.service.game.repository.GameResourceRepository;
import com.games.balancegameback.service.game.repository.GameResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GameListRepositoryImpl implements GameListRepository {

    private final GameResultRepository gameResultRepository;
    private final GameResourceRepository gameResourceRepository;
    private final GameRepository gameRepository;


}
