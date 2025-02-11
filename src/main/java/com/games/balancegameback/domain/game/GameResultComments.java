package com.games.balancegameback.domain.game;

import com.games.balancegameback.domain.user.Users;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.List;

public record GameResultComments(Long id, String comment, boolean like, Users users, Games games, Long parentId,
                                 LocalDateTime createdDate, LocalDateTime updatedDate, List<GameResultComments> children) {

    @Builder
    public GameResultComments {

    }
}
