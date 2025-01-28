package com.games.balancegameback.dto.game.gameplay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GamePlayResourceLinkResponse {

    @Schema(description = "유튜브 Link")
    private String link;

    @Schema(description = "유튜브 URL 시작 초")
    private int startSec;

    @Schema(description = "유튜브 URL 끝 초")
    private int endSec;
}
