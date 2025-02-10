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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private GameResourceCommentsEntity parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameResourceCommentsEntity> children = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_resources_id")
    private GameResourcesEntity gameResources;

    public static GameResourceCommentsEntity from(GameResourceComments gameResourceComments) {
        GameResourceCommentsEntity gameResourceCommentsEntity = new GameResourceCommentsEntity();
        gameResourceCommentsEntity.comment = gameResourceComments.getComment();
        gameResourceCommentsEntity.gameResources = GameResourcesEntity.from(gameResourceComments.getGameResources());

        // 부모 댓글 설정
        if (gameResourceComments.getParentId() != null) {
            GameResourceCommentsEntity parent = new GameResourceCommentsEntity();
            parent.id = gameResourceComments.getParentId();
            gameResourceCommentsEntity.parent = parent;
        }

        return gameResourceCommentsEntity;
    }

    public GameResourceComments toModel() {
        return GameResourceComments.builder()
                .id(id)
                .comment(comment)
                .parentId(parent != null ? parent.getId() : null)
                .gameResources(gameResources.toModel())
                .createdDate(this.getCreatedDate())
                .updatedDate(this.getUpdatedDate())
                .children(children.stream() // 대댓글 리스트 변환
                        .map(GameResourceCommentsEntity::toModel)
                        .toList())
                .build();
    }
}
