package com.games.balancegameback.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenResponse {

    private Long accessTokenExpiresAt;
    private Long refreshTokenExpiresAt;
}
