package com.games.balancegameback.dto.user;

import com.games.balancegameback.domain.user.enums.LoginType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequest {

    @Schema(description = "이메일")
    @NotBlank(message = "이메일은 비어 있을 수 없습니다.")
    private String email;

    @Schema(description = "로그인 종류", implementation = LoginType.class)
    @NotBlank(message = "로그인 타입은 비어 있을 수 없습니다.")
    private LoginType loginType;

    @Schema(description = "인증 토큰")
    @NotBlank(message = "토큰은 비어 있을 수 없습니다.")
    private String accessToken;
}
