package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.GameResultComments;
import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
@Table(name = "game_result_comments")
public class GameResultCommentsEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String comment;

    @Column
    private boolean like = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private UsersEntity users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "games_id", nullable = false)
    private GamesEntity games;

    public static GameResultCommentsEntity from(GameResultComments gameResultComments) {
        GameResultCommentsEntity gameResultCommentsEntity = new GameResultCommentsEntity();
        gameResultCommentsEntity.comment = gameResultComments.comment();
        gameResultCommentsEntity.like = gameResultComments.like();
        gameResultCommentsEntity.users = UsersEntity.from(gameResultComments.users());
        gameResultCommentsEntity.games = GamesEntity.from(gameResultComments.games());

        return gameResultCommentsEntity;
    }

    public GameResultComments toModel() {
        return GameResultComments.builder()
                .id(id)
                .comment(comment)
                .like(like)
                .users(users.toModel())
                .games(games.toModel())
                .createdDate(this.getCreatedDate())
                .updatedDate(this.getUpdatedDate())
                .build();
    }
}
