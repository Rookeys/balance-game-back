package com.games.balancegameback.core.converter;

import com.games.balancegameback.domain.game.enums.CommentSortType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@Component
public class CommentSortTypeConverter implements Converter<String, CommentSortType> {

    @Override
    @Nullable
    public CommentSortType convert(@Nullable String source) {
        if (source == null || source.isBlank()) {
            return CommentSortType.resent;
        }

        try {
            return CommentSortType.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CommentSortType.resent;
        }
    }
}
