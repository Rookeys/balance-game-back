package com.games.balancegameback.dto.game;

import com.games.balancegameback.domain.game.enums.AccessType;
import com.games.balancegameback.domain.game.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GameRequest {

    @Schema(description = "방 제목")
    @NotBlank(message = "방 제목은 필수입니다.")
    private String title;

    @Schema(description = "방 설명")
    private String description;

    @Schema(description = "익명 여부")
    @NotBlank(message = "익명 여부 결정은 필수입니다.")
    private boolean isNamePublic;

    @Schema(description = "접근 레벨 설정")
    @NotBlank(message = "접근 레벨 설정은 필수입니다.")
    private AccessType accessType;

    @Schema(description = "초대 코드")
    private String inviteCode;

    @Schema(description = "카테고리 설정")
    @NotBlank(message = "카테고리 설정은 필수입니다.")
    private Category category;
}
