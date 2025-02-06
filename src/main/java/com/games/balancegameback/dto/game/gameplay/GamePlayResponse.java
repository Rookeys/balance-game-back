package com.games.balancegameback.dto.game.gameplay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GamePlayResponse {

    @Schema(description = "게임방 id")
    private Long id;

    @Schema(description = "왼쪽 리소스 정보")
    private GamePlayResourceResponse leftResource;

    @Schema(description = "오른쪽 리소스 정보")
    private GamePlayResourceResponse rightResource;
}
