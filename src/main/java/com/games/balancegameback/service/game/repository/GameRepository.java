package com.games.balancegameback.service.game.repository;

import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.GameListResponse;
import com.games.balancegameback.dto.game.GameResponse;
import com.games.balancegameback.dto.game.GameSearchRequest;
import org.springframework.data.domain.Pageable;

public interface GameRepository {

    Games save(Games games);

    GameResponse findById(Long roomId);

    Games findByRoomId(Long roomId);

    CustomPageImpl<GameListResponse> findGamesWithResources(Long cursorId, Users users, Pageable pageable, GameSearchRequest searchRequest);

    boolean existsIdAndUsers(Long gameId, Users users);

    boolean existsGameRounds(Long gameId, int roundNumber);

    void update(Games games);

    void deleteById(Long roomId);

    void deleteImagesInS3(Long roomId);
}
