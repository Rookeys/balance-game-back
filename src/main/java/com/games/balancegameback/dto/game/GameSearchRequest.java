package com.games.balancegameback.dto.game;

import com.games.balancegameback.domain.game.enums.Category;
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

    @Schema(description = "검색하려는 타이틀")
    private String title;

    @Schema(description = "정렬 옵션", implementation = GameSortType.class, name = "GameSortType")
    private GameSortType sortType = GameSortType.recent;

    @Schema(description = "카테고리", implementation = Category.class, name = "Category")
    private Category category;
}
