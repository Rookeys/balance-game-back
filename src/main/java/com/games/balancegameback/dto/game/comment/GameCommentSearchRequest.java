package com.games.balancegameback.dto.game.comment;

import com.games.balancegameback.domain.game.enums.CommentSortType;
import com.games.balancegameback.domain.game.enums.RecentSearchType;
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

    @Schema(description = "검색하려는 내용", example = "포메")
    private String content;

    @Schema(description = "정렬 옵션", allowableValues = {"likeAsc", "likeDesc", "idAsc", "idDesc"}, example = "likeDesc")
    private CommentSortType sortType;

    @Schema(description = "기간 옵션", allowableValues = {"DAY", "WEEK", "MONTH"}, example = "WEEK")
    private RecentSearchType recentSearchType;
}
