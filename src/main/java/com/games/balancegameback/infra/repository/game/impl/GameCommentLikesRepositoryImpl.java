package com.games.balancegameback.infra.repository.game.impl;

import com.games.balancegameback.infra.repository.game.GameCommentLikesJpaRepository;
import com.games.balancegameback.service.game.repository.GameCommentLikesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class GameCommentLikesRepositoryImpl implements GameCommentLikesRepository {

    private final GameCommentLikesJpaRepository gameCommentLikesJpaRepository;


}
