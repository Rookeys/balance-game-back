package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.GameCommentLikes;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "game_resource_comment_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"users_id", "comment_id"}) // 중복 좋아요 방지
})
public class GameCommentLikesEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private UsersEntity users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_resource_comments_id")
    private GameResourceCommentsEntity resourceComments;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_result_comments_id")
    private GameResultCommentsEntity resultComments;

    public static GameCommentLikesEntity from(GameCommentLikes gameCommentLikes) {
        GameCommentLikesEntity entity = new GameCommentLikesEntity();
        entity.id = gameCommentLikes.getId();
        entity.users = UsersEntity.from(gameCommentLikes.getUsers());

        if (gameCommentLikes.getResourceComments() != null) {
            entity.resourceComments = GameResourceCommentsEntity.from(gameCommentLikes.getResourceComments());
        }

        if (gameCommentLikes.getResultComments() != null) {
            entity.resultComments = GameResultCommentsEntity.from(gameCommentLikes.getResultComments());
        }

        return entity;
    }

    public GameCommentLikes toModel() {
        return GameCommentLikes.builder()
                .id(id)
                .users(users.toModel())
                .resourceComments(resourceComments != null ? resourceComments.toModel() : null)
                .resultComments(resultComments != null ? resultComments.toModel() : null)
                .build();
    }
}

