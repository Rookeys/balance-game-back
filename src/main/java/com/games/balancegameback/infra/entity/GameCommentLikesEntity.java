package com.games.balancegameback.infra.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "game_resource_comment_likes", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"users_id", "comment_id"}) // 중복 좋아요 방지
})
public class GameCommentLikesEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id")
    private UsersEntity users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_resource_comments_id", nullable = false)
    private GameResourceCommentsEntity resourceComments;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_result_comments_id", nullable = false)
    private GameResultCommentsEntity resultComments;
}

