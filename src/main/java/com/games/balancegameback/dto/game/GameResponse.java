package com.games.balancegameback.dto.game;

import com.games.balancegameback.domain.game.enums.AccessType;
import com.games.balancegameback.domain.game.enums.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GameResponse {

    @Schema(description = "게임방 ID")
    private Long roomId;

    @Schema(description = "게임 타이틀")
    private String title;

    @Schema(description = "설명")
    private String description;

    @Schema(description = "익명 여부", name = "isNamePrivate")
    private boolean namePrivate;

    @Schema(description = "썸네일 블라인드 여부", name = "isBlind")
    private boolean blind;

    @Schema(description = "접근 권한")
    private AccessType accessType;

    @Schema(description = "초대 코드")
    private String inviteCode;

    @Schema(description = "카테고리 설정", name = "categories")
    private List<Category> categories;
}
