package com.games.balancegameback.dto.game;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameResourceResponse {

    @Schema(description = "리소스 ID")
    private Long resourceId;

    @Schema(description = "리소스 제목")
    private String title;

    @Schema(description = "이미지 URL")
    private String fileUrl;

    @Schema(description = "유튜브 Link")
    private String link;

    @Schema(description = "유튜브 URL 시작 초")
    private int startSec;

    @Schema(description = "유튜브 URL 끝 초")
    private int endSec;

    @Schema(description = "우승 비율")
    private double winRate;
}
