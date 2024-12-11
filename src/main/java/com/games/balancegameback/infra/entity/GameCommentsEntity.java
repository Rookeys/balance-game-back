package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.GameComments;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Table(name = "game_comments")
public class GameCommentsEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String comment;

    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime createdDate;

    @Column
    @LastModifiedDate
    private LocalDateTime updatedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private GameCommentsEntity parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<GameCommentsEntity> children = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "games_id")
    private GamesEntity games;

    public static GameCommentsEntity from(GameComments gameComments) {
        GameCommentsEntity gameCommentsEntity = new GameCommentsEntity();
        gameCommentsEntity.comment = gameComments.comment();
        gameCommentsEntity.games = GamesEntity.from(gameComments.games());

        // 부모 댓글 설정
        if (gameComments.parentId() != null) {
            GameCommentsEntity parent = new GameCommentsEntity();
            parent.id = gameComments.parentId();
            gameCommentsEntity.parent = parent;
        }

        return gameCommentsEntity;
    }

    public GameComments toModel() {
        return GameComments.builder()
                .id(id)
                .comment(comment)
                .parentId(parent != null ? parent.getId() : null)
                .games(games.toModel())
                .createdDate(createdDate)
                .updatedDate(updatedDate)
                .children(children.stream() // 대댓글 리스트 변환
                        .map(GameCommentsEntity::toModel)
                        .toList())
                .build();
    }
}
