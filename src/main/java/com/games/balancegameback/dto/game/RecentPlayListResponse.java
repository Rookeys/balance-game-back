package com.games.balancegameback.dto.game;

import com.games.balancegameback.domain.game.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecentPlayListResponse {

    @Schema(description = "게임방 ID")
    private Long roomId;

    @Schema(description = "게임 타이틀")
    private String title;

    @Schema(description = "내가 선택한 리소스 타이틀")
    private String resourceTitle;

    @Schema(description = "썸네일 블라인드 여부")
    private Boolean existsBlind;

    @Schema(description = "설명")
    private String description;

    @Schema(description = "카테고리", name = "categories")
    private List<Category> categories;

    @Schema(description = "썸네일 이미지 URL")
    private String thumbnailImageUrl;

    @Schema(description = "썸네일 유튜브 URL")
    private String thumbnailLinkUrl;

    @Schema(description = "썸네일 타입 (IMAGE, LINK)")
    private String thumbnailType;
}
