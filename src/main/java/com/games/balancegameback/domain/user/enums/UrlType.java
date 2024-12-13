package com.games.balancegameback.domain.user.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UrlType {

    KAKAO("KAKAO", "https://kapi.kakao.com/v2/user/me"),
    GOOGLE("GOOGLE", "https://www.googleapis.com/oauth2/v2/userinfo");

    private final String key;
    private final String title;
}
