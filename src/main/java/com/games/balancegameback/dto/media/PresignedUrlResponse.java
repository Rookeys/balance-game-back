package com.games.balancegameback.dto.media;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PresignedUrlResponse {

    @Schema(description = "S3 업로드용 Presigned URL (PUT 전용, 10분 유효)")
    private String uploadUrl;

    @Schema(description = "DB 저장용 클린 S3 URL (만료 없음)")
    private String fileUrl;
}
