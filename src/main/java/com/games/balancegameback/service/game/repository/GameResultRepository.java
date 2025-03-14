package com.games.balancegameback.service.game.repository;

import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.GameResults;
import com.games.balancegameback.dto.game.GameResourceSearchRequest;
import com.games.balancegameback.dto.game.GameResultResponse;
import org.springframework.data.domain.Pageable;

public interface GameResultRepository {

    CustomPageImpl<GameResultResponse> findGameResultRanking(Long gameId, Long cursorId, GameResourceSearchRequest request,
                                                             Pageable pageable);

    int countByGameId(Long roomId);

    int countByGameResourcesId(Long resourcesId);

    void save(GameResults gameResults);
}
