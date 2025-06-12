package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.GameResourceComments;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "game_resource_comments", indexes = {
        @Index(name = "idx_resource_comments_resource_id", columnList = "game_resource_id"),
        @Index(name = "idx_resource_comments_user_id", columnList = "user_id"),
        @Index(name = "idx_resource_comments_parent_id", columnList = "parent_id")
})
public class GameResourceCommentsEntity extends BaseTimeEntity {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "game_resource_id", nullable = false, length = 36)
    private String gameResourceId;

    @Column(name = "parent_id")
    private Long parentId;

    public static GameResourceCommentsEntity from(GameResourceComments gameResourceComments) {
        GameResourceCommentsEntity entity = new GameResourceCommentsEntity();
        entity.id = gameResourceComments.getId();
        entity.comment = gameResourceComments.getComment();
        entity.isDeleted = gameResourceComments.isDeleted();
        entity.userId = gameResourceComments.getUsers().getUid();
        entity.gameResourceId = gameResourceComments.getGameResources().getId();
        entity.parentId = gameResourceComments.getParentId();
        return entity;
    }

    public GameResourceComments toModel() {
        return GameResourceComments.builder()
                .id(id)
                .comment(comment)
                .isDeleted(isDeleted)
                .parentId(parentId)
                .createdDate(this.getCreatedDate())
                .updatedDate(this.getUpdatedDate())
                .build();
    }

    public void update(GameResourceComments comments) {
        this.comment = comments.getComment();
        this.isDeleted = comments.isDeleted();
    }
}