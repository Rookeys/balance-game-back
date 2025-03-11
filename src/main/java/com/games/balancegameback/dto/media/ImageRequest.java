package com.games.balancegameback.dto.media;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ImageRequest {

    @Schema(description = "S3에 저장된 이미지 URL")
    @NotBlank(message = "URL 은 필수입니다.")
    private List<String> urls;
}
