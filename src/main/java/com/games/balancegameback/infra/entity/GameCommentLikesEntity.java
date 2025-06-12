package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.GameCommentLikes;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "game_comment_likes",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"user_id", "resource_comment_id"}),
                @UniqueConstraint(columnNames = {"user_id", "result_comment_id"})
        },
        indexes = {
                @Index(name = "idx_comment_likes_user_id", columnList = "user_id"),
                @Index(name = "idx_comment_likes_resource_comment_id", columnList = "resource_comment_id"),
                @Index(name = "idx_comment_likes_result_comment_id", columnList = "result_comment_id")
        }
)
public class GameCommentLikesEntity extends BaseTimeEntity {

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "resource_comment_id")
    private Long resourceCommentId;

    @Column(name = "result_comment_id")
    private Long resultCommentId;

    public static GameCommentLikesEntity from(GameCommentLikes gameCommentLikes) {
        GameCommentLikesEntity entity = new GameCommentLikesEntity();
        entity.id = gameCommentLikes.getId();
        entity.userId = gameCommentLikes.getUsers().getUid();

        if (gameCommentLikes.getResourceComments() != null) {
            entity.resourceCommentId = gameCommentLikes.getResourceComments().getId();
        }

        if (gameCommentLikes.getResultComments() != null) {
            entity.resultCommentId = gameCommentLikes.getResultComments().getId();
        }

        return entity;
    }

    public GameCommentLikes toModel() {
        return GameCommentLikes.builder()
                .id(id)
                .build();
    }
}

