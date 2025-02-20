package com.games.balancegameback.web.game;

import com.games.balancegameback.dto.game.gameplay.GamePlayRequest;
import com.games.balancegameback.dto.game.gameplay.GamePlayResponse;
import com.games.balancegameback.dto.game.gameplay.GamePlayRoundRequest;
import com.games.balancegameback.service.game.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/games")
@Tag(name = "Game Play Controller", description = "Game Play CRUD API")
public class GamePlayController {

    private final GameService gameService;

    @Operation(summary = "플레이룸 생성 및 게임 시작 API", description = "n강만큼 데이터가 준비되고 첫 2개의 데이터를 반환.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "플레이룸 생성 완료"),
            @ApiResponse(responseCode = "400", description = "초대 코드가 null 입니다.")
    })
    @PostMapping(value = "/{gameId}/play")
    public ResponseEntity<GamePlayResponse> createPlayRoom(
            @Parameter(name = "gameId", description = "게임방의 ID", required = true, example = "3")
            @PathVariable(name = "gameId") Long gameId,

            @RequestBody @Valid GamePlayRoundRequest gamePlayRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(gameService.createPlayRoom(gameId, gamePlayRequest));
    }

    @Operation(summary = "플레이룸 결과 반영 API", description = "선택한 리소스를 업데이트하고 다음 페어를 반환.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "플레이룸 업데이트 완료"),
            @ApiResponse(responseCode = "400", description = "이미 선택한 리소스입니다."),
            @ApiResponse(responseCode = "404", description = "일치하는 리소스 ID가 없습니다."),
    })
    @PutMapping(value = "/{gameId}/play/{playId}")
    public ResponseEntity<GamePlayResponse> updatePlayRoom(
            @Parameter(name = "gameId", description = "게임방의 ID", required = true, example = "3")
            @PathVariable(name = "gameId") Long gameId,

            @Parameter(name = "playId", description = "플레이룸의 ID", required = true, example = "6")
            @PathVariable(name = "playId") Long playId,

            @RequestBody @Valid GamePlayRequest gamePlayRequest) {
        return ResponseEntity.status(HttpStatus.OK).body(gameService.updatePlayRoom(gameId, playId, gamePlayRequest));
    }
}
