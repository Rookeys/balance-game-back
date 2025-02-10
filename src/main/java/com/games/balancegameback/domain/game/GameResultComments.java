package com.games.balancegameback.domain.game;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public record GameResultComments(Long id, String comment, Games games, Long parentId,
                                 LocalDateTime createdDate, LocalDateTime updatedDate, List<GameResultComments> children) {

    @Builder
    public GameResultComments {

    }
}
