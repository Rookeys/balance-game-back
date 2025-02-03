package com.games.balancegameback.dto.game;

import com.games.balancegameback.domain.media.enums.MediaType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GameResultResponse {

    @Schema(description = "리소스 ID")
    private Long resourceId;

    @Schema(description = "리소스 제목")
    private String title;

    @Schema(description = "Image 인지 Youtube Link 인지 구분하는 Enum")
    private MediaType type;

    @Schema(description = "Image / Youtube Link")
    private String content;

    @Schema(description = "Youtube Link 시작 시간")
    private Integer startSec;

    @Schema(description = "Youtube Link 끝 시간")
    private Integer endSec;

    @Schema(description = "우승 비율")
    private Double winRate;
}
