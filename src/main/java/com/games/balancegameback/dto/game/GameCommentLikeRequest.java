package com.games.balancegameback.dto.game;

import com.games.balancegameback.domain.game.enums.CommentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameCommentLikeRequest {

    @Schema(description = "좋아요 신청/취소")
    @NotNull
    private boolean existsLiked;

    @Schema(description = "댓글 종류", implementation = CommentType.class)
    @NotNull
    private CommentType sortType;
}
