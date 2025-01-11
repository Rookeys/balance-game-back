package com.games.balancegameback.dto.game;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GameListResponse {

    @Schema(description = "게임방 ID")
    private Long roomId;

    @Schema(description = "게임 타이틀")
    private String title;

    @Schema(description = "설명")
    private String description;

    @Schema(description = "왼쪽 선택지 FileUrl / Link")
    private String leftContent;

    @Schema(description = "오른쪽 선택지 FileUrl / Link")
    private String rightContent;

    @Schema(description = "왼쪽 선택지 설명")
    private String leftTitle;

    @Schema(description = "오른쪽 선택지 설명")
    private String rightTitle;
}
