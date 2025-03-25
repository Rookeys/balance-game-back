package com.games.balancegameback.dto.game;

import com.games.balancegameback.domain.game.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameCategoryNumsResponse {

    @Schema(description = "전체 게임 수")
    private Integer totalNums;

    @Schema(description = "각 카테고리에 포함된 게임 수")
    private Map<Category, Long> categoryNums;
}
