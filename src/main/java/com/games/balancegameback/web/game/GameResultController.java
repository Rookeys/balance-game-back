package com.games.balancegameback.web.game;

import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.enums.GameResourceSortType;
import com.games.balancegameback.dto.game.GameResourceSearchRequest;
import com.games.balancegameback.dto.game.GameResultResponse;
import com.games.balancegameback.service.game.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/games")
@Tag(name = "Game Results Controller", description = "Game Results CRUD API")
public class GameResultController {

    private final GameService gameService;

    @Operation(summary = "게임 결과창 출력 API", description = "해당 게임방의 결과창을 출력함.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결과창 출력 성공")
    })
    @GetMapping(value = "/{gameId}/results")
    public CustomPageImpl<GameResultResponse> getResultRanking(
            @Parameter(name = "gameId", description = "게임방의 ID", required = true, example = "3")
            @PathVariable(name = "gameId") Long gameId,

            @Parameter(name = "cursorId", description = "커서 ID", example = "4")
            @RequestParam(name = "cursorId", required = false) Long cursorId,

            @Parameter(name = "size", description = "한 페이지 당 출력 개수", example = "10")
            @RequestParam(name = "size", required = false, defaultValue = "15") int size,

            @Parameter(name = "title", description = "검색할 리소스 제목", example = "포메")
            @RequestParam(name = "title", required = false) String title,

            @Parameter(name = "sortType", description = "정렬 방식",
                    example = "winRateDesc",
                    schema = @Schema(allowableValues = {"winRateAsc", "winRateDesc", "idAsc", "idDesc"}))
            @RequestParam(name = "sortType", required = false, defaultValue = "idDesc") GameResourceSortType sortType) {

        Pageable pageable = PageRequest.of(0, size);
        GameResourceSearchRequest searchRequest = GameResourceSearchRequest.builder()
                .title(title)
                .sortType(sortType)
                .build();

        return gameService.getResultRanking(gameId, cursorId, searchRequest, pageable);
    }
}
