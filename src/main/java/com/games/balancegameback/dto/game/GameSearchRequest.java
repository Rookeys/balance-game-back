package com.games.balancegameback.dto.game;

import com.games.balancegameback.domain.game.enums.GameSortType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameSearchRequest {

    @Schema(description = "검색하려는 타이틀", example = "포메")
    private String title;

    @Schema(description = "정렬 옵션", allowableValues = {"playRateAsc", "playRateDesc", "idAsc", "idDesc"},
            example = "winRateAsc")
    private GameSortType sortType = GameSortType.idDesc;
}
