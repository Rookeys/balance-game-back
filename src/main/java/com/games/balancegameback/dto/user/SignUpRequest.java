package com.games.balancegameback.dto.user;

import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.domain.user.enums.LoginType;
import com.games.balancegameback.domain.user.enums.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
public class SignUpRequest {

    @Schema(description = "닉네임")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$", message = "닉네임에는 특수 문자를 포함할 수 없습니다.")
    @NotBlank(message = "닉네임은 비어 있을 수 없습니다.")
    private String nickname;

    @Schema(description = "이메일")
    @NotBlank(message = "이메일은 비어 있을 수 없습니다.")
    private String email;

    @Schema(description = "로그인 타입", example = "KAKAO / GOOGLE")
    @NotBlank(message = "로그인 타입은 비어 있을 수 없습니다.")
    private LoginType loginType;

    @Schema(description = "소셜 로그인 측 서버에서 발급받은 토큰")
    @NotBlank(message = "토큰은 비어 있을 수 없습니다.")
    private String accessToken;

    @Schema(description = "사진 URL")
    private String image;

    public Users toDomain() {
        return Users.builder()
                .nickname(nickname)
                .email(email)
                .loginType(loginType)
                .userRole(UserRole.USER)
                .build();
    }
}
