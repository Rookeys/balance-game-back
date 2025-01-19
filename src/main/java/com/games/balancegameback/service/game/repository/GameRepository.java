package com.games.balancegameback.service.game.repository;

import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.GameListResponse;
import com.games.balancegameback.dto.game.GameResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GameRepository {

    Games save(Games games);

    GameResponse findById(Long roomId);

    Games findByRoomId(Long roomId);

    Page<GameListResponse> findGamesWithResources(Long cursorId, Users users, Pageable pageable);

    boolean existsByIdAndUsers(Long gameId, Users users);

    void update(Games games);

    void deleteById(Long roomId);
}
