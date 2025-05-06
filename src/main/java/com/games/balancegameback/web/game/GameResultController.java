package com.games.balancegameback.web.game;

import com.games.balancegameback.core.utils.CustomBasedPageImpl;
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
import jakarta.validation.constraints.Min;
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

    @Operation(summary = "각 리소스 별 승률 목록 출력 API", description = "해당 게임방의 리소스 별 승률 목록을 출력함.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "각 리소스 별 승률 목록 출력 성공")
    })
    @GetMapping(value = "/{gameId}/results")
    public CustomPageImpl<GameResultResponse> getResultRanking(
            @Parameter(name = "gameId", description = "게임방의 ID", required = true)
            @PathVariable(name = "gameId") Long gameId,

            @Parameter(name = "cursorId", description = "커서 ID")
            @RequestParam(name = "cursorId", required = false) Long cursorId,

            @Parameter(name = "size", description = "한 페이지 당 출력 개수")
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,

            @Parameter(name = "title", description = "검색할 리소스 제목")
            @RequestParam(name = "title", required = false) String title,

            @Parameter(name = "sortType", description = "정렬 방식",
                    schema = @Schema(implementation = GameResourceSortType.class))
            @RequestParam(name = "sortType", required = false, defaultValue = "RECENT") GameResourceSortType sortType) {

        Pageable pageable = PageRequest.of(0, size);
        GameResourceSearchRequest searchRequest = GameResourceSearchRequest.builder()
                .title(title)
                .sortType(sortType)
                .build();

        return gameService.getResultRanking(gameId, cursorId, searchRequest, pageable);
    }

    @Operation(summary = "각 리소스 별 승률 목록 출력 API (page 기반)", description = "해당 게임방의 리소스 별 승률 목록을 출력함.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "각 리소스 별 승률 목록 출력 성공")
    })
    @GetMapping(value = "/{gameId}/results/page")
    public CustomBasedPageImpl<GameResultResponse> getResultRankingUsingPage(
            @Parameter(name = "gameId", description = "게임방의 ID", required = true)
            @PathVariable(name = "gameId") Long gameId,

            @Parameter(name = "page", description = "페이지 번호")
            @RequestParam(name = "page", defaultValue = "1")
            @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.") int page,

            @Parameter(name = "size", description = "한 페이지 당 출력 개수")
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,

            @Parameter(name = "title", description = "검색할 리소스 제목")
            @RequestParam(name = "title", required = false) String title,

            @Parameter(name = "sortType", description = "정렬 방식",
                    schema = @Schema(implementation = GameResourceSortType.class))
            @RequestParam(name = "sortType", required = false, defaultValue = "RECENT") GameResourceSortType sortType) {

        Pageable pageable = PageRequest.of(page - 1, size);
        GameResourceSearchRequest searchRequest = GameResourceSearchRequest.builder()
                .title(title)
                .sortType(sortType)
                .build();

        return gameService.getResultRankingUsingPage(gameId, pageable, searchRequest);
    }

}
