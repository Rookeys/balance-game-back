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
public class GameListSelectionResponse {

    @Schema(description = "리소스 ID")
    private Long id;

    @Schema(description = "선택지 타이틀")
    private String title;

    @Schema(description = "미디어 타입")
    private MediaType type;

    @Schema(description = "이미지 / 유튜브 링크 URL")
    private String content;

    @Schema(description = "유튜브 URL 시작 초")
    private int startSec;

    @Schema(description = "유튜브 URL 끝 초")
    private int endSec;
}
