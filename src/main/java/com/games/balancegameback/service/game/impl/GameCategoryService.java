package com.games.balancegameback.service.game.impl;

import com.games.balancegameback.domain.game.GameCategory;
import com.games.balancegameback.domain.game.Games;
import com.games.balancegameback.domain.game.enums.Category;
import com.games.balancegameback.service.game.repository.GameCategoryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameCategoryService {

    private final GameCategoryRepository gameCategoryRepository;

    public void saveCategory(List<Category> categories, Games games) {
        for (Category category : categories) {
            GameCategory gameCategory = GameCategory.builder()
                    .category(category)
                    .games(games)
                    .build();

            gameCategoryRepository.save(gameCategory);
        }
    }

    @Transactional
    public void updateCategory(List<Category> categories, Games games) {
        gameCategoryRepository.deleteAll(games.getId());

        for (Category category : categories) {
            GameCategory gameCategory = GameCategory.builder()
                    .category(category)
                    .games(games)
                    .build();

            gameCategoryRepository.update(gameCategory);
        }
    }
}
