package com.games.balancegameback.core.converter;

import com.games.balancegameback.domain.game.enums.Category;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.lang.Nullable;

@Component
public class CategoryConverter implements Converter<String, Category> {

    @Override
    @Nullable
    public Category convert(@Nullable String source) {
        if (source == null || source.isBlank()) {
            return null;
        }

        try {
            return Category.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

