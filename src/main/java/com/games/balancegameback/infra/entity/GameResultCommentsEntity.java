package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.GameResultComments;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "game_result_comments")
public class GameResultCommentsEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_uid", nullable = false)
    private UsersEntity users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "games_id", nullable = false)
    private GamesEntity games;

    @OneToMany(mappedBy = "resultComments", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameCommentLikesEntity> likes = new ArrayList<>();

    public static GameResultCommentsEntity from(GameResultComments gameResultComments) {
        GameResultCommentsEntity gameResultCommentsEntity = new GameResultCommentsEntity();
        gameResultCommentsEntity.id = gameResultComments.getId();
        gameResultCommentsEntity.comment = gameResultComments.getComment();
        gameResultCommentsEntity.users = UsersEntity.from(gameResultComments.getUsers());
        gameResultCommentsEntity.games = GamesEntity.from(gameResultComments.getGames());

        return gameResultCommentsEntity;
    }

    public GameResultComments toModel() {
        return GameResultComments.builder()
                .id(id)
                .comment(comment)
                .users(users.toModel())
                .games(games.toModel())
                .createdDate(this.getCreatedDate())
                .updatedDate(this.getUpdatedDate())
                .likes(likes)
                .build();
    }

    public void update(GameResultComments gameResultComments) {
        this.comment = gameResultComments.getComment();
    }
}
