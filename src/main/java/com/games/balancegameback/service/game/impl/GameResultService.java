package com.games.balancegameback.service.game.impl;

import com.games.balancegameback.core.utils.CustomBasedPageImpl;
import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.dto.game.GameResourceSearchRequest;
import com.games.balancegameback.dto.game.GameResultResponse;
import com.games.balancegameback.service.game.repository.GameResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameResultService {

    private final GameResultRepository gameResultRepository;

    public CustomPageImpl<GameResultResponse> getResultRanking(Long gameId, Long cursorId,
                                                               GameResourceSearchRequest request,
                                                               Pageable pageable) {
        return gameResultRepository.findGameResultRanking(gameId, cursorId, request, pageable);
    }

    public CustomBasedPageImpl<GameResultResponse> getResultRankingUsingPage(Long gameId, Pageable pageable,
                                                                             GameResourceSearchRequest request) {
        return gameResultRepository.findGameResultRankingWithPaging(gameId, pageable, request);
    }
}
