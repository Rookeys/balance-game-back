package com.games.balancegameback.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FollowUserResponse {

    @Schema(description = "닉네임")
    private String nickname;

    @Schema(description = "이메일")
    private String email;

    @Schema(description = "프로필 사진 URL")
    private String fileUrl;

    @Schema(description = "팔로우 여부")
    private Boolean isFollowing;

    @Schema(description = "팔로우 버튼 표시 여부")
    private Boolean showFollowButton;
}
