package com.games.balancegameback.dto.media;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class PresignedUrlsRequest {

    @Schema(description = "prefix")
    private String prefix;

    @Schema(description = "파일 이름")
    private List<String> fileNameList = new ArrayList<>();
}
