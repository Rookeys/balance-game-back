package com.games.balancegameback.dto.game.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameResultCommentResponse {

    @Schema(description = "댓글 ID")
    private Long commentId;

    @Schema(description = "댓글 내용")
    private String comment;

    @Schema(description = "작성자 닉네임")
    private String nickname;

    @Schema(description = "작성 시간")
    private LocalDateTime createdDateTime;

    @Schema(description = "수정 시간")
    private LocalDateTime updatedDateTime;

    @Schema(description = "좋아요 총합")
    private int like;

    @Schema(description = "좋아요 클릭 유무")
    private boolean isLiked;
}
