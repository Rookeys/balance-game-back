package com.games.balancegameback.dto.game.gameplay;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GamePlayRoundRequest {

    @Schema(description = "n강")
    @NotBlank(message = "n강 선택은 필수입니다.")
    private int roundNumber;
}
