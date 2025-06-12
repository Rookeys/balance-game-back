package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.GameResultComments;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "game_result_comments", indexes = {
        @Index(name = "idx_result_comments_game_id", columnList = "game_id"),
        @Index(name = "idx_result_comments_user_id", columnList = "user_id")
})
public class GameResultCommentsEntity extends BaseTimeEntity {

    @Column(nullable = false, columnDefinition = "TEXT")
    private String comment;

    @Column(name = "user_id", nullable = false, length = 36)
    private String userId;

    @Column(name = "game_id", nullable = false, length = 36)
    private String gameId;

    public static GameResultCommentsEntity from(GameResultComments gameResultComments) {
        GameResultCommentsEntity entity = new GameResultCommentsEntity();
        entity.id = gameResultComments.getId();
        entity.comment = gameResultComments.getComment();
        entity.userId = gameResultComments.getUsers().getUid();
        entity.gameId = gameResultComments.getGames().getId();
        return entity;
    }

    public GameResultComments toModel() {
        return GameResultComments.builder()
                .id(id)
                .comment(comment)
                .createdDate(this.getCreatedDate())
                .updatedDate(this.getUpdatedDate())
                .build();
    }

    public void update(GameResultComments gameResultComments) {
        this.comment = gameResultComments.getComment();
    }
}
