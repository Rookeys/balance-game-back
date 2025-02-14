package com.games.balancegameback.service.game.repository;

import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.GameResourceComments;
import com.games.balancegameback.dto.game.comment.GameCommentSearchRequest;
import com.games.balancegameback.dto.game.comment.GameResourceChildrenCommentResponse;
import com.games.balancegameback.dto.game.comment.GameResourceParentCommentResponse;
import org.springframework.data.domain.Pageable;

public interface GameResourceCommentRepository {

    void save(GameResourceComments gameResourceComments);

    GameResourceComments findById(Long id);

    void update(GameResourceComments gameResourceComments);

    void delete(GameResourceComments gameResourceComments);

    CustomPageImpl<GameResourceParentCommentResponse> findByGameResourceComments(Long resourceId, Long cursorId,
                                                                                 Pageable pageable,
                                                                                 GameCommentSearchRequest request);

    CustomPageImpl<GameResourceChildrenCommentResponse> findByGameResourceChildrenComments(Long parentId, Long cursorId,
                                                                                   Pageable pageable,
                                                                                   GameCommentSearchRequest request);

    boolean existsByParentId(Long id);
}
