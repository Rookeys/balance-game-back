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
    private Long playId;

    @Schema(description = "선택한 n강")
    private int totalRoundNums;

    @Schema(description = "현재 진행 중인 라운드")
    private int currentRoundNums;

    @Schema(description = "왼쪽 리소스 정보")
    private GamePlayResourceResponse leftResource;

    @Schema(description = "오른쪽 리소스 정보")
    private GamePlayResourceResponse rightResource;

    @Schema(description = "오른쪽 리소스 정보")
    private GamePlayWinningResourceResponse winningResource;
}
