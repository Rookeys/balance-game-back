package com.games.balancegameback.service.game.impl;

import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.*;
import com.games.balancegameback.service.game.repository.GameListRepository;
import com.games.balancegameback.service.user.impl.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GameListService {

    private final GameListRepository gameListRepository;
    private final UserUtils userUtils;

    public CustomPageImpl<GameListResponse> getMainGameList(Long cursorId, Pageable pageable,
                                                            GameSearchRequest searchRequest) {
        return gameListRepository.getGameList(cursorId, pageable, searchRequest);
    }

    public GameCategoryNumsResponse getCategoryNums(String title) {
        return gameListRepository.getCategoryCounts(title);
    }

    public GameDetailResponse getGameStatus(Long gameId, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        return gameListRepository.getGameStatus(gameId, users);
    }
}
