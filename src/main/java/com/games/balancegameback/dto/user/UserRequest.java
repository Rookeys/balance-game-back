package com.games.balancegameback.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserRequest {

    @Schema(description = "닉네임", example = "TestUser")
    @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$", message = "닉네임에는 특수 문자를 포함할 수 없습니다.")
    private String nickname;

    @Schema(description = "프로필 사진 URL",
            example = "https://dessert-gallery.s3.ap-northeast-2.amazonaws.com/image/1ec7ecbf-e2c7-48df-a77a-7a4a467db466")
    private String url;
}
