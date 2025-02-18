package com.games.balancegameback.service.game.repository;

import com.games.balancegameback.domain.game.GameCommentLikes;

public interface GameCommentLikesRepository {

    void save(GameCommentLikes gameCommentLikes);

    void deleteByUsersEmailAndResourceCommentsId(String email, Long commentId);

    void deleteByUsersEmailAndResultCommentsId(String email, Long commentId);
}
