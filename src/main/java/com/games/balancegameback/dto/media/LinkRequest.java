package com.games.balancegameback.dto.media;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LinkRequest {

    @Schema(description = "유튜브 URL")
    @NotBlank(message = "URL 은 적어도 하나 이상 있어야 합니다.")
    private String url;

    @Schema(description = "시작 초")
    private int startSec;

    @Schema(description = "끝 초")
    private int endSec;
}
