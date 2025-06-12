package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.GameCategory;
import com.games.balancegameback.domain.game.enums.Category;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "game_categories", indexes = {
        @Index(name = "idx_game_categories_game_id", columnList = "game_id")
})
public class GameCategoryEntity extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private Category category;

    @Column(name = "game_id", nullable = false, length = 36)
    private String gameId;

    @Override
    protected String getEntityPrefix() {
        return "GCT";
    }

    @PrePersist
    public void prePersist() {
        generateId();
    }

    public static GameCategoryEntity from(GameCategory gameCategory) {
        GameCategoryEntity entity = new GameCategoryEntity();
        entity.id = gameCategory.getId();
        entity.category = gameCategory.getCategory();
        entity.gameId = gameCategory.getGames().getId();
        return entity;
    }

    public GameCategory toModel() {
        return GameCategory.builder()
                .id(id)
                .category(category)
                .build();
    }
}