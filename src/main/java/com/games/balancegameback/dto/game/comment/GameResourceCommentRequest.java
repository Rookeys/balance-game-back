package com.games.balancegameback.dto.game.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GameResourceCommentRequest {

    @Schema(description = "부모 댓글 Id", example = "3")
    private Long parentId;

    @Schema(description = "댓글")
    @NotBlank(message = "댓글 내용은 필수입니다.")
    private String comment;
}
