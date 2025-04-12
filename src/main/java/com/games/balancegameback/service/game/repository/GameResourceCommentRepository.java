package com.games.balancegameback.service.game.repository;

import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.GameResourceComments;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.comment.GameCommentSearchRequest;
import com.games.balancegameback.dto.game.comment.GameResourceChildrenCommentResponse;
import com.games.balancegameback.dto.game.comment.GameResourceParentCommentResponse;
import org.springframework.data.domain.Pageable;

public interface GameResourceCommentRepository {

    void save(GameResourceComments gameResourceComments);

    GameResourceComments findById(Long id);

    void update(GameResourceComments gameResourceComments);

    void delete(GameResourceComments gameResourceComments);

    CustomPageImpl<GameResourceParentCommentResponse> findByGameResourceComments(Long gameId, Long resourceId, Long cursorId,
                                                                                 Users users, Pageable pageable,
                                                                                 GameCommentSearchRequest request);

    CustomPageImpl<GameResourceChildrenCommentResponse> findByGameResourceChildrenComments(Long gameId, Long resourceId,
                                                                                           Long parentId, Long cursorId,
                                                                                           Users users, Pageable pageable,
                                                                                           GameCommentSearchRequest request);

    boolean existsByGameIdAndResourceIdAndCommentId(Long gameId, Long resourceId, Long commentId);

    boolean existsById(Long commentId);

    boolean isChildComment(Long parentId);

    boolean existsByGameIdAndResourceId(Long gameId, Long resourceId);

    boolean existsByGameIdAndCommentId(Long gameId, Long commentId);
}
