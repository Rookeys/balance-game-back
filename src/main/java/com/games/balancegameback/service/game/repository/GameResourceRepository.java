package com.games.balancegameback.service.game.repository;

import com.games.balancegameback.domain.game.GameResources;
import com.games.balancegameback.dto.game.GameResourceResponse;
import com.games.balancegameback.dto.game.GameResourceSearchRequest;
import com.games.balancegameback.dto.game.gameplay.GamePlayResourceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GameResourceRepository {

    void save(GameResources gameResources);

    void update(GameResources gameResources);

    GameResources findById(Long id);

    List<GamePlayResourceResponse> findByIds(List<Long> ids);

    List<Long> findByRandomId(Long gameId, int roundNumber);

    Page<GameResourceResponse> findByGameId(Long gameId, Long cursorId, Pageable pageable, GameResourceSearchRequest request);

    void deleteById(Long id);
}
