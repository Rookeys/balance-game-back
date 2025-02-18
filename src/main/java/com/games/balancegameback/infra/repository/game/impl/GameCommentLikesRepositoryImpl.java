package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.domain.game.GameCommentLikes;
import com.games.balancegameback.infra.entity.GameCommentLikesEntity;
import com.games.balancegameback.infra.repository.game.GameCommentLikesJpaRepository;
import com.games.balancegameback.service.game.repository.GameCommentLikesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GameCommentLikesRepositoryImpl implements GameCommentLikesRepository {

    private final GameCommentLikesJpaRepository gameCommentLikesJpaRepository;

    @Override
    public void save(GameCommentLikes gameCommentLikes) {
        gameCommentLikesJpaRepository.save(GameCommentLikesEntity.from(gameCommentLikes));
    }

    @Override
    public void deleteByUsersEmailAndResourceCommentsId(String email, Long commentId) {
        gameCommentLikesJpaRepository.deleteByUsersEmailAndResourceCommentsId(email, commentId);
    }

    @Override
    public void deleteByUsersEmailAndResultCommentsId(String email, Long commentId) {
        gameCommentLikesJpaRepository.deleteByUsersEmailAndResultCommentsId(email, commentId);
    }
}
