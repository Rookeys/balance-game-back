package com.games.balancegameback.web.game;

import com.games.balancegameback.dto.game.GameResultResponse;
import com.games.balancegameback.service.game.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
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
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "결과창 출력 성공")
    })
    @GetMapping(value = "/{gameId}/results")
    public Page<GameResultResponse> getResultRanking(
            @Parameter(name = "gameId", description = "게임방의 ID", required = true, example = "3")
            @PathVariable(name = "gameId") Long gameId,

            @Parameter(name = "cursorId", description = "커서 ID", example = "4")
            @RequestParam(name = "cursorId") Long cursorId,

            @Parameter(name = "searchQuery", description = "검색 키워드", example = "포메")
            @RequestParam(name = "searchQuery") String searchQuery) {
        Pageable pageable = PageRequest.of(0, 15);
        return gameService.getResultRanking(gameId, cursorId, searchQuery, pageable);
    }
}
