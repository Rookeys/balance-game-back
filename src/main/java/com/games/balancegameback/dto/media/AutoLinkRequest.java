package com.games.balancegameback.dto.media;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AutoLinkRequest {

    @Schema(description = "유튜브 URL")
    @NotBlank(message = "URL 은 필수입니다.")
    private String url;

    @Schema(description = "유튜브 URL 타이틀")
    @NotBlank(message = "타이틀은 필수입니다.")
    private String title;

    @Schema(description = "시작 초")
    private int startSec;

    @Schema(description = "끝 초")
    private int endSec;
}
