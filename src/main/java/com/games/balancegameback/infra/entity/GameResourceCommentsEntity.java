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

    @Column
    private boolean like = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "users_id", nullable = false)
    private UsersEntity users;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private GameResourceCommentsEntity parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<GameResourceCommentsEntity> children = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_resources_id")
    private GameResourcesEntity gameResources;

    public static GameResourceCommentsEntity from(GameResourceComments gameResourceComments) {
        GameResourceCommentsEntity gameResourceCommentsEntity = new GameResourceCommentsEntity();
        gameResourceCommentsEntity.id = gameResourceComments.getId();
        gameResourceCommentsEntity.comment = gameResourceComments.getComment();
        gameResourceCommentsEntity.like = gameResourceComments.isLike();
        gameResourceCommentsEntity.gameResources = GameResourcesEntity.from(gameResourceComments.getGameResources());

        // 부모 댓글 설정
        if (gameResourceComments.getParentId() != null) {
            GameResourceCommentsEntity parantComments = new GameResourceCommentsEntity();
            parantComments.id = gameResourceComments.getParentId();
            parantComments.comment = gameResourceComments.getComment();
            parantComments.like = gameResourceComments.isLike();

            gameResourceCommentsEntity.parent = parantComments;
        } else {
            gameResourceCommentsEntity.parent = null;
            gameResourceCommentsEntity.comment = gameResourceComments.getComment();
        }

        return gameResourceCommentsEntity;
    }

    public GameResourceComments toModel() {
        return GameResourceComments.builder()
                .id(id)
                .comment(comment)
                .like(like)
                .parentId(parent != null ? parent.getId() : null)
                .gameResources(gameResources.toModel())
                .createdDate(this.getCreatedDate())
                .updatedDate(this.getUpdatedDate())
                .children(children.isEmpty() ? null : children.stream() // 대댓글 리스트 변환
                        .map(GameResourceCommentsEntity::toModel)
                        .toList())
                .build();
    }
}
