package com.games.balancegameback.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserRequest {

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "프로필 사진 URL")
    private String url;
}
