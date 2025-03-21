package com.games.balancegameback.dto.game;

import com.games.balancegameback.domain.game.enums.Category;
import com.games.balancegameback.dto.user.UserMainResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameListResponse {

    @Schema(description = "게임방 ID")
    private Long roomId;

    @Schema(description = "게임 타이틀")
    private String title;

    @Schema(description = "설명")
    private String description;

    @Schema(description = "썸네일 블라인드 여부")
    private Boolean isBlind;

    @Schema(description = "카테고리", name = "Category")
    private Category category;

    @Schema(description = "총 플레이 횟수")
    private int totalPlayNums;

    @Schema(description = "HOT 태그를 위한 플레이 횟수")
    private int weekPlayNums;

    @Schema(description = "제작일")
    private OffsetDateTime createdAt;

    @Schema(description = "유저 정보")
    private UserMainResponse userResponse;

    @Schema(description = "왼쪽 선택지")
    private GameListSelectionResponse leftSelection;

    @Schema(description = "오른쪽 선택지")
    private GameListSelectionResponse rightSelection;
}
