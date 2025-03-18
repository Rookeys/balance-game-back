package com.games.balancegameback.dto.game;

import com.games.balancegameback.domain.game.enums.CommentType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
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
    @NotBlank
    private boolean isLiked;

    @Schema(description = "댓글 종류", implementation = CommentType.class)
    @NotBlank
    private CommentType sortType;
}
