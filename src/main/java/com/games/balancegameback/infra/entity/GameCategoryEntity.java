package com.games.balancegameback.infra.entity;

import com.games.balancegameback.domain.game.GameCategory;
import com.games.balancegameback.domain.game.enums.Category;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "game_categories")
public class GameCategoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "games_id")
    private GamesEntity games;

    public static GameCategoryEntity from(GameCategory gameCategory) {
        GameCategoryEntity entity = new GameCategoryEntity();
        entity.id = gameCategory.getId();
        entity.category = gameCategory.getCategory();
        entity.games = GamesEntity.from(gameCategory.getGames());

        return entity;
    }

    public GameCategory toModel() {
        return GameCategory.builder()
                .id(id)
                .category(category)
                .games(this.games.toModel())
                .build();
    }
}
