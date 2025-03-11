package com.games.balancegameback.service.game.repository;

import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.GameResources;
import com.games.balancegameback.dto.game.GameResourceResponse;
import com.games.balancegameback.dto.game.GameResourceSearchRequest;
import com.games.balancegameback.dto.game.gameplay.GamePlayResourceResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface GameResourceRepository {

    void save(GameResources gameResources);

    GameResources findById(Long id);

    List<GamePlayResourceResponse> findByIds(List<Long> ids);

    List<Long> findByRandomId(Long gameId, int roundNumber);

    CustomPageImpl<GameResourceResponse> findByGameId(Long gameId, Long cursorId, Pageable pageable,
                                                      GameResourceSearchRequest request);

    void update(GameResources gameResources);

    Integer countByGameId(Long gameId);

    void deleteById(Long id);
}
