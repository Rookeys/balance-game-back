package com.games.balancegameback.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserFollowRequest {

    @Schema(description = "닉네임")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$", message = "닉네임에는 특수 문자를 포함할 수 없습니다.")
    @NotBlank(message = "닉네임은 비어 있을 수 없습니다.")
    private String nickname;
}
