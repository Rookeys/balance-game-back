package com.games.balancegameback.service.game.repository;

import com.games.balancegameback.domain.game.GameResources;
import com.games.balancegameback.dto.game.GameResourceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GameResourceRepository {

    void save(GameResources gameResources);

    void update(GameResources gameResources);

    GameResources findById(Long id);

    Page<GameResourceResponse> findByGameId(Long gameId, Long cursorId, Pageable pageable);

    void deleteById(Long id);
}
