package com.games.balancegameback.dto.media;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PresignedUrlRequest {

    @Schema(description = "닉네임")
    private String prefix;

    @Schema(description = "이메일")
    private String fileName;
}
