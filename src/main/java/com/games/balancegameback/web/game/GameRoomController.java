package com.games.balancegameback.web.game;

import com.games.balancegameback.dto.game.GameListResponse;
import com.games.balancegameback.service.game.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/game")
@Tag(name = "Game Room Controller", description = "Game Room CRUD API")
public class GameRoomController {

    private final GameService gameService;

    @Operation(summary = "내가 만든 게임 리스트 확인", description = "내가 만든 게임들을 무한 스크롤 형식으로 확인 가능.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게임 내역 발급 성공")
    })
    @PostMapping(value = "/list")
    public Page<GameListResponse> getMyGameList(@RequestParam(required = false) Long cursorId,
                                                @PageableDefault(size = 15) Pageable pageable,
                                                HttpServletRequest request) {
        return gameService.getMyGameList(pageable, cursorId, request);
    }
}
