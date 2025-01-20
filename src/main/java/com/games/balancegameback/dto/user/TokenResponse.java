package com.games.balancegameback.dto.user;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TokenResponse {

    private Long accessTokenExpiresAt;
    private Long refreshTokenExpiresAt;
}
