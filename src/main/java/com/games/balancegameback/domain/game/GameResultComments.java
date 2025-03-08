package com.games.balancegameback.domain.game;

import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.infra.entity.GameCommentLikesEntity;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.List;

@Data
public class GameResultComments {

    private Long id;
    private String comment;
    private Users users;
    private Games games;
    private OffsetDateTime createdDate;
    private OffsetDateTime updatedDate;
    private List<GameCommentLikesEntity> likes;

    @Builder
    public GameResultComments(Long id, String comment, Users users,Games games,
                              OffsetDateTime createdDate, OffsetDateTime updatedDate,
                              List<GameCommentLikesEntity> likes) {
        this.id = id;
        this.comment = comment;
        this.users = users;
        this.games = games;
        this.createdDate = createdDate;
        this.updatedDate = updatedDate;
        this.likes = likes;
    }

    public void update(String comment) {
        this.comment = comment;
    }
}
