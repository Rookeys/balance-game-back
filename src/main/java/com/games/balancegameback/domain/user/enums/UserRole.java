package com.games.balancegameback.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserRole {

    USER("USER", "일반 사용자"),
    ADMIN("ADMIN", "관리자");

    private final String key;
    private final String title;
}
