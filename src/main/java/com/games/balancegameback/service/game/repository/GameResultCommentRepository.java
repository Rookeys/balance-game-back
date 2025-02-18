package com.games.balancegameback.service.game.repository;

import com.games.balancegameback.domain.game.GameResultComments;

public interface GameResultCommentRepository {

    void save(GameResultComments gameResultComments);

    GameResultComments findById(Long id);
}
