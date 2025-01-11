package com.games.balancegameback.web.game;

import com.games.balancegameback.dto.game.GameListResponse;
import com.games.balancegameback.dto.game.GameRequest;
import com.games.balancegameback.dto.game.GameResponse;
import com.games.balancegameback.service.game.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/game")
@Tag(name = "Game Room Controller", description = "Game Room CRUD API")
public class GameRoomController {

    private final GameService gameService;

    @Operation(summary = "게임방 생성 API", description = "게임방의 기본적인 설정들을 받아 생성함.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "게임방 생성 완료"),
            @ApiResponse(responseCode = "400", description = "초대 코드가 null 입니다.")
    })
    @PostMapping(value = "")
    public ResponseEntity<Long> saveGame(
            @RequestBody GameRequest gameRequest,
            HttpServletRequest request) {
        Long id = gameService.saveGame(gameRequest, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(id);
    }

    @Operation(summary = "게임방 정보 확인 API", description = "특정 게임방의 설정을 확인함.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게임방 설정 내역 발급 성공"),
            @ApiResponse(responseCode = "401", description = "게임 주인이 아닙니다.")
    })
    @GetMapping(value = "")
    public GameResponse getGameStatus(
            @Parameter(name = "roomId", description = "게임방의 ID", required = true, example = "3")
            @RequestParam(name = "roomId") Long roomId,

            HttpServletRequest request) {
        return gameService.getGameStatus(roomId, request);
    }

    @Operation(summary = "내가 만든 게임 리스트 확인 API", description = "내가 만든 게임들을 무한 스크롤 형식으로 확인 가능.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "내가 만든 게임 리스트 발급 성공")
    })
    @GetMapping(value = "/list")
    public Page<GameListResponse> getMyGameList(
            @Parameter(name = "cursorId", description = "커서 ID (페이징 처리용)", example = "15")
            @RequestParam(name = "cursorId", required = false) Long cursorId,

            @PageableDefault(size = 15) Pageable pageable,
            HttpServletRequest request) {
        return gameService.getMyGameList(pageable, cursorId, request);
    }

    @Operation(summary = "게임방 설정 업데이트 API", description = "게임방의 설정들을 변경 가능.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게임방 수정 완료"),
            @ApiResponse(responseCode = "400", description = "필수값이 비어 있습니다."),
            @ApiResponse(responseCode = "401", description = "게임 주인이 아닙니다.")
    })
    @PutMapping(value = "")
    public ResponseEntity<String> updateGameStatus(
            @Parameter(name = "roomId", description = "게임방의 ID", required = true, example = "3")
            @RequestParam(name = "roomId") Long roomId,

            @RequestBody @Valid GameRequest gameRequest,
            HttpServletRequest request) {
        gameService.updateGameStatus(roomId, gameRequest, request);
        return ResponseEntity.ok("게임방 설정값 수정 완료.");
    }

    @Operation(summary = "게임방 삭제 API", description = "게임방을 삭제 가능.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "게임방 삭제 완료"),
            @ApiResponse(responseCode = "401", description = "게임 주인이 아닙니다.")
    })
    @DeleteMapping(value = "")
    public ResponseEntity<String> deleteGame(
            @Parameter(name = "roomId", description = "게임방의 ID", required = true, example = "3")
            @RequestParam(name = "roomId") Long roomId,

            HttpServletRequest request) {
        gameService.deleteGame(roomId, request);
        return ResponseEntity.ok("게임방 삭제 완료.");
    }
}

