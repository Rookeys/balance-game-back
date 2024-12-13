package com.games.balancegameback.dto.user;

import com.games.balancegameback.domain.user.enums.LoginType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LoginRequest {

    @Schema(description = "닉네임")
    @NotBlank(message = "닉네임은 비어 있을 수 없습니다.")
    private String nickname;

    @Schema(description = "이메일")
    @NotBlank(message = "이메일은 비어 있을 수 없습니다.")
    private String email;

    @Schema(description = "로그인 종류", example = "KAKAO / GOOGLE")
    @NotBlank(message = "로그인 타입은 비어 있을 수 없습니다.")
    private LoginType loginType;

    @Schema(description = "인증 코드")
    @NotBlank(message = "인증 코드는 비어 있을 수 없습니다.")
    private String code;
}
