package com.games.balancegameback.domain.game;

import com.games.balancegameback.domain.game.enums.Category;
import lombok.Builder;
import lombok.Data;

@Data
public class GameCategory {

    private Long id;
    private Category category;
    private Games games;

    @Builder
    public GameCategory(Long id, Category category, Games games) {
        this.id = id;
        this.category = category;
        this.games = games;
    }

    public void update(Category category) {
        this.category = category;
    }
}
