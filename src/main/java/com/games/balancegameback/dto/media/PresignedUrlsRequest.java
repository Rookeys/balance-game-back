package com.games.balancegameback.dto.media;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PresignedUrlsRequest {

    @Schema(description = "prefix")
    private String prefix;

    @Schema(description = "업로드 개수")
    private int length;
}
