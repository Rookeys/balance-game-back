package com.games.balancegameback.domain.media.enums;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MediaType {
    IMAGE("IMAGE", "이미지"),
    LINK("LINK", "유튜브 링크");

    private final String value;
    private final String desc;

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static MediaType from(String code) {
        if (code == null) {
            return null;
        }

        for (MediaType status : MediaType.values()) {
            if (status.value.equals(code)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Unknown MediaType code: " + code);
    }
}
