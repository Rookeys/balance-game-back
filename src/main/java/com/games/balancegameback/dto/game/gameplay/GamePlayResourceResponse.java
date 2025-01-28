package com.games.balancegameback.dto.game.gameplay;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GamePlayResourceResponse {

    @Schema(description = "리소스 ID")
    private Long resourceId;

    @Schema(description = "리소스 제목")
    private String title;

    @Schema(description = "이미지 URL")
    private String fileUrl;

    @Schema(description = "유튜브 Link")
    private GamePlayResourceLinkResponse gameResourceLink;
}
