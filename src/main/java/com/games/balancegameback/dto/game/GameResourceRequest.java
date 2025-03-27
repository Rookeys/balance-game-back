package com.games.balancegameback.dto.game;

import com.games.balancegameback.domain.media.enums.MediaType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GameResourceRequest {

    @Schema(description = "리소스 제목")
    private String title;

    @Schema(description = "미디어 타입")
    private MediaType type;

    @Schema(description = "이미지 / 유튜브 링크 URL")
    private String content;

    @Schema(description = "유튜브 URL 시작 초 설정")
    private int startSec;

    @Schema(description = "유튜브 URL 끝 초 설정")
    private int endSec;
}
