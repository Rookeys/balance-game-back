package com.games.balancegameback.dto.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse {

    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpiresAt;
    private Long refreshTokenExpiresAt;
}
