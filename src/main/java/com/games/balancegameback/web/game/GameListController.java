package com.games.balancegameback.web.game;

import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.enums.Category;
import com.games.balancegameback.domain.game.enums.GameSortType;
import com.games.balancegameback.dto.game.GameListResponse;
import com.games.balancegameback.dto.game.GameSearchRequest;
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
@Tag(name = "Main Page Controller", description = "Main Page's API")
public class GameListController {

    private final GameService gameService;

    @Operation(summary = "메인 페이지 리스트 발급 API", description = "메인 페이지 리스트 목록을 제공한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "발급 완료")
    })
    @GetMapping(value = "/list")
    public CustomPageImpl<GameListResponse> getMainGameList(

            @Parameter(name = "cursorId", description = "커서 ID (페이징 처리용)")
            @RequestParam(name = "cursorId", required = false) Long cursorId,

            @Parameter(name = "size", description = "한 페이지 당 출력 개수")
            @RequestParam(name = "size", required = false, defaultValue = "15") int size,

            @Parameter(name = "title", description = "검색할 리소스 제목")
            @RequestParam(name = "title", required = false) String title,

            @Parameter(name = "category", description = "카테고리",
                    schema = @Schema(implementation = Category.class, name = "Category"))
            @RequestParam(name = "category", required = false) Category category,

            @Parameter(name = "sortType", description = "정렬 방식",
                    schema = @Schema(implementation = GameSortType.class, name = "GameSortType"))
            @RequestParam(name = "sortType", required = false, defaultValue = "RECENT") GameSortType sortType) {

        Pageable pageable = PageRequest.of(0, size);
        GameSearchRequest searchRequest = GameSearchRequest.builder()
                .title(title)
                .sortType(sortType)
                .category(category)
                .build();

        return gameService.getMainGameList(cursorId, pageable, searchRequest);
    }
}
