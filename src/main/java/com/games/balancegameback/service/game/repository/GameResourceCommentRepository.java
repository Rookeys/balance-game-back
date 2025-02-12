package com.games.balancegameback.service.game.repository;

import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.GameResourceComments;
import com.games.balancegameback.dto.game.GameResourceSearchRequest;
import com.games.balancegameback.dto.game.comment.GameResourceCommentResponse;
import org.springframework.data.domain.Pageable;

public interface GameResourceCommentRepository {

    void save(GameResourceComments gameResourceComments);

    GameResourceComments findById(Long id);

    void update(GameResourceComments gameResourceComments);

    void delete(GameResourceComments gameResourceComments);

    CustomPageImpl<GameResourceCommentResponse> findByGameResourceComments(Long resourceId, Long cursorId, Pageable pageable, GameResourceSearchRequest request);
}
