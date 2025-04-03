package com.games.balancegameback.service.game.repository;

import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.dto.game.GameCategoryNumsResponse;
import com.games.balancegameback.dto.game.GameDetailResponse;
import com.games.balancegameback.dto.game.GameListResponse;
import com.games.balancegameback.dto.game.GameSearchRequest;
import org.springframework.data.domain.Pageable;

public interface GameListRepository {

    CustomPageImpl<GameListResponse> getGameList(Long cursorId, Pageable pageable, GameSearchRequest searchRequest);

    GameCategoryNumsResponse getCategoryCounts(String title);

    GameDetailResponse getGameStatus(Long gameId);
}
