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
public class GameListSelectionResponse {

    @Schema(description = "선택지 타이틀")
    private String title;

    @Schema(description = "이미지 / 유튜브 링크 URL")
    private String content;
}
