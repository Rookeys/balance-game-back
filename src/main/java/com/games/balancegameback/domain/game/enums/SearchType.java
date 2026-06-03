/*
 * File Name   : SearchType.java
 * Description : 게임 리스트 검색 타입 enum
 *
 * Created By  : cheomuk
 * Created At  : 2026-06-03
 * Updated At  : 2026-06-03
 *
 * Change Log
 * -------------------------------------------------
 * 2026-06-03  최초 생성
 */
package com.games.balancegameback.domain.game.enums;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

public enum SearchType {
    TITLE,
    NICKNAME;

    @Component
    public static class StringConverter implements Converter<String, SearchType> {

        @Override
        @Nullable
        public SearchType convert(@Nullable String source) {
            if (source == null || source.isBlank()) {
                return SearchType.TITLE;
            }
            try {
                return SearchType.valueOf(source.toUpperCase());
            } catch (IllegalArgumentException e) {
                return SearchType.TITLE;
            }
        }
    }
}
