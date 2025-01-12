package com.games.balancegameback.dto.game;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GameResourceRequest {

    @Schema(description = "리소스 제목")
    private String title;

    @Schema(description = "바꾸고자 하는 이미지 URL")
    private String fileUrl;

    @Schema(description = "바꾸고자 하는 유튜브 URL")
    private String link;

    @Schema(description = "유튜브 URL 시작 초 설정")
    private int startSec;

    @Schema(description = "유튜브 URL 끝 초 설정")
    private int endSec;
}
