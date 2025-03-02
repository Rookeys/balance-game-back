package com.games.balancegameback.service.game.impl;

import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.dto.game.GameListResponse;
import com.games.balancegameback.dto.game.GameSearchRequest;
import com.games.balancegameback.service.game.repository.GameListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameListService {

    private final GameListRepository gameListRepository;

    public CustomPageImpl<GameListResponse> getMainGameList(Long cursorId, Pageable pageable,
                                                            GameSearchRequest searchRequest) {
        return gameListRepository.getGameList(cursorId, pageable, searchRequest);
    }
}
