package com.games.balancegameback.service.game.repository;

import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.GameResultComments;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.game.comment.GameCommentSearchRequest;
import com.games.balancegameback.dto.game.comment.GameResultCommentResponse;
import org.springframework.data.domain.Pageable;

public interface GameResultCommentRepository {

    void save(GameResultComments gameResultComments);

    void update(GameResultComments gameResultComments);

    void delete(GameResultComments gameResultComments);

    CustomPageImpl<GameResultCommentResponse> findByGameResultComments(Long gameId, Long cursorId, Users users,
                                                                       Pageable pageable,
                                                                       GameCommentSearchRequest searchRequest);

    GameResultComments findById(Long id);
}
