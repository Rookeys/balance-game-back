package com.games.balancegameback.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "이메일")
    private String email;

    @Schema(description = "프로필 사진 URL")
    private String fileUrl;

    @Schema(description = "액세스 토큰")
    private String accessToken;

    @Schema(description = "리프레쉬 토큰")
    private String refreshToken;

    @Schema(description = "액세스 토큰 만료 시간")
    private Long accessTokenExpiresAt;

    @Schema(description = "리프레쉬 토큰 만료 시간")
    private Long refreshTokenExpiresAt;
}
