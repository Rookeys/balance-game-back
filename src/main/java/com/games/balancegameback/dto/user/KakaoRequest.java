package com.games.balancegameback.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class KakaoRequest {

    @Schema(description = "인증 코드")
    @NotBlank(message = "인증 코드는 비어 있을 수 없습니다.")
    private String authorizeCode;
}
