package com.games.balancegameback.dto.game;

import com.games.balancegameback.domain.media.enums.MediaType;
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

    @Schema(description = "미디어 타입")
    private MediaType type;

    @Schema(description = "사진 / 유튜브 URL")
    private String content;

    @Schema(description = "유튜브 URL 시작 초")
    private int startSec;

    @Schema(description = "유튜브 URL 끝 초")
    private int endSec;

    @Schema(description = "우승 횟수")
    private int winningNums;

    @Schema(description = "게임 진행 횟수")
    private int totalPlayNums;
}
