package com.games.balancegameback.dto.game.gameplay;

import com.games.balancegameback.domain.game.enums.AccessType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameInfoResponse {

    @Schema(description = "게임 제목")
    private String title;

    @Schema(description = "설명")
    private String description;

    @Schema(description = "총 리소스 갯수")
    private int totalResourceNums;

    @Schema(description = "접근 권한")
    private AccessType accessType;
}
