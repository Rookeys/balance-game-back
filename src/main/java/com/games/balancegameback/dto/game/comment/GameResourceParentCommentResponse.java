package com.games.balancegameback.dto.game.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameResourceParentCommentResponse {

    @Schema(description = "댓글 ID")
    private Long commentId;

    @Schema(description = "댓글 내용")
    private String comment;

    @Schema(description = "작성자 닉네임")
    private String nickname;

    @Schema(description = "작성자 프로필 사진")
    private String profileImageUrl;

    @Schema(description = "대댓글 개수")
    private int children;

    @Schema(description = "작성 시간")
    private OffsetDateTime createdDateTime;

    @Schema(description = "수정 시간")
    private OffsetDateTime updatedDateTime;

    @Schema(description = "삭제 여부")
    private boolean isDeleted;

    @Schema(description = "좋아요 총합")
    private int like;

    @Schema(description = "좋아요 클릭 유무")
    private boolean existsLiked;

    @Schema(description = "작성자 본인 확인")
    private boolean existsWriter;
}
