package com.games.balancegameback.dto.game.gameplay;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GamePlayRequest {

    @Schema(description = "선택한 리소스 ID")
    @NotBlank(message = "ID 값은 필수입니다.")
    private Long winResourceId;

    @Schema(description = "선택받지 못한 리소스 ID")
    @NotBlank(message = "ID 값은 필수입니다.")
    private Long loseResourceId;
}
