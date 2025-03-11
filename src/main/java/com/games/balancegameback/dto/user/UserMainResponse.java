package com.games.balancegameback.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMainResponse {

    @Schema(description = "제작자 닉네임")
    private String nickname;

    @Schema(description = "프로필 사진 URL")
    private String profileImageUrl;
}
