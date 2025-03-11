package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.GameResourceComments;
import jakarta.persistence.*;
import lombok.Getter;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "game_resource_comments")
public class GameResourceCommentsEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String comment;

    @Column(nullable = false)
    private boolean isDeleted = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_uid")
    private UsersEntity users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_resources_id")
    private GameResourcesEntity gameResources;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private GameResourceCommentsEntity parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameResourceCommentsEntity> children = new ArrayList<>();

    @OneToMany(mappedBy = "resourceComments", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameCommentLikesEntity> likes = new ArrayList<>();

    public static GameResourceCommentsEntity from(GameResourceComments gameResourceComments) {
        GameResourceCommentsEntity entity = new GameResourceCommentsEntity();
        entity.id = gameResourceComments.getId();
        entity.comment = gameResourceComments.getComment();
        entity.isDeleted = gameResourceComments.isDeleted();
        entity.users = UsersEntity.from(gameResourceComments.getUsers());
        entity.gameResources = GameResourcesEntity.from(gameResourceComments.getGameResources());

        if (gameResourceComments.getParentId() != null) {
            GameResourceCommentsEntity parent = new GameResourceCommentsEntity();
            parent.id = gameResourceComments.getParentId();
            entity.parent = parent;
        }

        return entity;
    }

    public GameResourceComments toModel() {
        return GameResourceComments.builder()
                .id(id)
                .comment(comment)
                .isDeleted(isDeleted)
                .users(users.toModel())
                .parentId(parent != null ? parent.getId() : null)
                .gameResources(gameResources.toModel())
                .createdDate(this.getCreatedDate())
                .updatedDate(this.getUpdatedDate())
                .children(children.stream()
                        .map(GameResourceCommentsEntity::toModel)
                        .toList())
                .likes(likes)
                .build();
    }

    public void update(GameResourceComments comments) {
        this.comment = comments.getComment();
        this.isDeleted = comments.isDeleted();
    }
}
