package com.games.balancegameback.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FollowCountResponse {

    @Schema(description = "팔로워 수", example = "150")
    private long followerCount;

    @Schema(description = "팔로잉 수", example = "200")
    private long followingCount;
}
