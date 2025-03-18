package com.games.balancegameback.core.converter;

import com.games.balancegameback.domain.game.enums.GameSortType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.lang.Nullable;

@Component
public class GameSortTypeConverter implements Converter<String, GameSortType> {

    @Override
    @Nullable
    public GameSortType convert(@Nullable String source) {
        if (source == null || source.isBlank()) {
            return GameSortType.recent;
        }

        try {
            return GameSortType.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            return GameSortType.recent;
        }
    }
}

