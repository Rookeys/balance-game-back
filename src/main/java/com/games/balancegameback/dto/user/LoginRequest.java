package com.games.balancegameback.dto.user;

import com.games.balancegameback.domain.user.enums.LoginType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequest {

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "이메일")
    private String email;

    @Schema(description = "로그인 종류", example = "KAKAO / GOOGLE")
    private LoginType loginType;

    @Schema(description = "인증 코드")
    private String code;
}
