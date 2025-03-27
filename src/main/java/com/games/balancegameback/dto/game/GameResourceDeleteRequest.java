package com.games.balancegameback.dto.game;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class GameResourceDeleteRequest {

    @Schema(description = "삭제 리스트")
    private List<Long> list = new ArrayList<>();
}
