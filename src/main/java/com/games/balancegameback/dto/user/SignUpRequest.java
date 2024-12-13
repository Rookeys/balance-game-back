package com.games.balancegameback.dto.user;

import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.domain.user.enums.LoginType;
import com.games.balancegameback.domain.user.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

public class SignUpRequest {

    @Schema(description = "닉네임")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$", message = "닉네임에는 특수 문자를 포함할 수 없습니다.")
    private String nickname;

    @Schema(description = "이메일")
    private String email;

    @Schema(description = "로그인 종류", example = "KAKAO / GOOGLE")
    private LoginType loginType;

    @Schema(description = "인증 코드")
    private String code;

    @Schema(description = "사진 URL")
    private String url;

    public Users toDomain() {
        return Users.builder()
                .uid(String.valueOf(UUID.randomUUID()))
                .nickname(nickname)
                .email(email)
                .loginType(loginType)
                .userRole(UserRole.USER)
                .build();
    }
}
