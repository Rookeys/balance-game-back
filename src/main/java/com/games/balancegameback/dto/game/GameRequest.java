package com.games.balancegameback.dto.game;

import com.games.balancegameback.domain.game.enums.AccessType;
import com.games.balancegameback.domain.game.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class GameRequest {

    @Schema(description = "방 제목")
    @NotBlank(message = "방 제목은 필수입니다")
    private String title;

    @Schema(description = "방 설명")
    @NotBlank(message = "게임 설명은 필수입니다")
    private String description;

    @Schema(description = "익명 여부")
    @NotNull(message = "익명 여부 결정은 필수입니다")
    private boolean existsNamePrivate;

    @Schema(description = "썸네일 블라인드 여부")
    @NotNull(message = "썸네일 블라인드 여부 결정은 필수입니다")
    private boolean existsBlind;

    @Schema(description = "접근 레벨 설정")
    @NotNull(message = "접근 레벨 설정은 필수입니다")
    private AccessType accessType;

    @Schema(description = "초대 코드")
    private String inviteCode = "";

    @Schema(description = "카테고리 설정", name = "categories")
    @Size(min = 1, message = "최소 1개의 카테고리를 선택해야 합니다")
    private List<Category> categories;
}
