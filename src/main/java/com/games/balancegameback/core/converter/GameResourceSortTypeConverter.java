package com.games.balancegameback.core.converter;

import com.games.balancegameback.domain.game.enums.GameResourceSortType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class GameResourceSortTypeConverter implements Converter<String, GameResourceSortType> {

    @Override
    @Nullable
    public GameResourceSortType convert(@Nullable String source) {
        if (source == null || source.isBlank()) {
            return GameResourceSortType.RESENT;
        }

        try {
            return GameResourceSortType.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            return GameResourceSortType.RESENT;
        }
    }
}
