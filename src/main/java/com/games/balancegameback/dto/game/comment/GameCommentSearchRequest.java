package com.games.balancegameback.dto.game.comment;

import com.games.balancegameback.domain.game.enums.CommentSortType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameCommentSearchRequest {

    @Schema(description = "검색하려는 내용")
    private String content;

    @Schema(description = "정렬 옵션", implementation = CommentSortType.class)
    private CommentSortType sortType;
}
