package com.games.balancegameback.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FollowRequest {

    @Schema(description = "팔로잉할 이메일")
    @Email(message = "유효한 이메일 형식이어야 합니다.")
    @NotBlank(message = "팔로잉할 이메일은 비어 있을 수 없습니다.")
    private String followingEmail;
}
