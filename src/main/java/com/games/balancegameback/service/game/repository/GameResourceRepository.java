package com.games.balancegameback.service.game.repository;

import com.games.balancegameback.domain.game.GameResources;
import com.games.balancegameback.dto.game.GameResourceResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GameResourceRepository {

    void save(GameResources gameResources);

    GameResources findById(Long id);

    Page<GameResourceResponse> findByRoomId(Long roomId, Long cursorId, Pageable pageable);

    void deleteById(Long id);
}
