package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.GameResourceComments;
import com.games.balancegameback.dto.game.GameResourceSearchRequest;
import com.games.balancegameback.dto.game.comment.GameResourceCommentResponse;
import com.games.balancegameback.infra.entity.GameResourceCommentsEntity;
import com.games.balancegameback.infra.repository.game.GameResourceCommentJpaRepository;
import com.games.balancegameback.service.game.repository.GameResourceCommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GameResourceCommentRepositoryImpl implements GameResourceCommentRepository {

    private final GameResourceCommentJpaRepository gameResourceCommentRepository;

    @Override
    public void save(GameResourceComments gameResourceComments) {
        gameResourceCommentRepository.save(GameResourceCommentsEntity.from(gameResourceComments));
    }

    @Override
    public GameResourceComments findById(Long id) {
        return gameResourceCommentRepository.findById(id).orElseThrow(() ->
                new NotFoundException("댓글이 존재하지 않습니다.", ErrorCode.NOT_FOUND_EXCEPTION)).toModel();
    }

    @Override
    public void update(GameResourceComments gameResourceComments) {
        GameResourceCommentsEntity entity = gameResourceCommentRepository.findById(gameResourceComments.getId())
                .orElseThrow(() -> new NotFoundException("해당하는 정보가 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));

        entity.update(gameResourceComments);
    }

    @Override
    public void delete(GameResourceComments gameResourceComments) {
        gameResourceCommentRepository.delete(GameResourceCommentsEntity.from(gameResourceComments));
    }

    @Override
    public CustomPageImpl<GameResourceCommentResponse> findByGameResourceComments(Long resourceId, Long cursorId,
                                                                                  Pageable pageable,
                                                                                  GameResourceSearchRequest request) {
        return null;
    }
}
