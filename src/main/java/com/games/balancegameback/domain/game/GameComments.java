package com.games.balancegameback.domain.game;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public record GameComments(Long id, String comment, Games games, Long parentId,
                           LocalDateTime createdDate, LocalDateTime updatedDate, List<GameComments> children) {

    @Builder
    public GameComments {

    }
}
