package com.games.balancegameback.service.game.impl;

import com.games.balancegameback.dto.game.GameResultResponse;
import com.games.balancegameback.service.game.repository.GameResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameResultService {

    private final GameResultRepository gameResultRepository;

    public Page<GameResultResponse> getResultRanking(Long gameId, Long cursorId, String searchQuery, Pageable pageable) {
        return gameResultRepository.findGameResultRanking(gameId, cursorId, searchQuery, pageable);
    }
}
