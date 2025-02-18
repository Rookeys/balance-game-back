package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.domain.game.GameResultComments;
import com.games.balancegameback.infra.entity.GameResultCommentsEntity;
import com.games.balancegameback.infra.repository.game.GameResultCommentJpaRepository;
import com.games.balancegameback.service.game.repository.GameResultCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GameResultCommentRepositoryImpl implements GameResultCommentRepository {

    private final GameResultCommentJpaRepository gameResultCommentJpaRepository;

    @Override
    public void save(GameResultComments gameResultComments) {
        gameResultCommentJpaRepository.save(GameResultCommentsEntity.from(gameResultComments));
    }

    @Override
    public GameResultComments findById(Long id) {
        return gameResultCommentJpaRepository.findById(id).orElseThrow(() ->
                new NotFoundException("해당하는 데이터가 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION)).toModel();
    }
}
