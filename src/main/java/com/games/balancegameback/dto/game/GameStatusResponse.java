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
public class GameStatusResponse {

    @Schema(description = "게임방 ID")
    private Long roomId;

    @Schema(description = "게임 타이틀")
    private String title;

    @Schema(description = "설명")
    private String description;
}
