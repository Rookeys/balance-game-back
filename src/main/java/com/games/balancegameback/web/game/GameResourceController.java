package com.games.balancegameback.web.game;

import com.games.balancegameback.dto.game.GameResourceRequest;
import com.games.balancegameback.dto.game.GameResourceResponse;
import com.games.balancegameback.service.game.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/game/resource")
@Tag(name = "Game Resource Controller", description = "Game Resource CRUD API")
public class GameResourceController {

    private final GameService gameService;

    @Operation(summary = "게임 리소스 리스트 발급 API", description = "해당 게임방의 리소스 목록을 제공한다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "발급 완료"),
            @ApiResponse(responseCode = "401", description = "게임룸 호스트가 아닙니다.")
    })
    @GetMapping(value = "")
    public Page<GameResourceResponse> getResources(
            @Parameter(name = "roomId", description = "게임방의 ID", required = true, example = "3")
            @RequestParam(name = "roomId") Long roomId,

            @Parameter(name = "cursorId", description = "커서 ID (페이징 처리용)", example = "15")
            @RequestParam(name = "cursorId", required = false) Long cursorId,

            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(0, 15);
        return gameService.getResources(roomId, cursorId, pageable, request);
    }

    @Operation(summary = "게임 리소스 수정 API", description = "리소스의 제목이나 URL 등을 수정할 수 있다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리소스 정보 수정 완료"),
            @ApiResponse(responseCode = "400", description = "resource & room ID 값은 필수입니다."),
            @ApiResponse(responseCode = "401", description = "게임룸 호스트가 아닙니다."),
            @ApiResponse(responseCode = "404", description = "해당 리소스는 없습니다.")
    })
    @PutMapping(value = "")
    public ResponseEntity<String> updateResource(
            @Parameter(name = "roomId", description = "게임방의 ID", required = true, example = "3")
            @RequestParam(name = "roomId") Long roomId,

            @Parameter(name = "resourceId", description = "등록된 리소스의 ID", required = true, example = "5")
            @RequestParam(name = "resourceId") Long resourceId,

            @RequestBody GameResourceRequest gameResourceRequest,
            HttpServletRequest request) {
        gameService.updateResource(roomId, resourceId, gameResourceRequest, request);
        return ResponseEntity.status(HttpStatus.OK).body("해당 리소스를 업데이트 했습니다.");
    }

    @Operation(summary = "게임 리소스 삭제 API", description = "등록된 리소스를 삭제할 수 있다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리소스 삭제 완료"),
            @ApiResponse(responseCode = "400", description = "resource & room ID 값은 필수입니다."),
            @ApiResponse(responseCode = "401", description = "게임룸 호스트가 아닙니다."),
            @ApiResponse(responseCode = "404", description = "해당 리소스는 없습니다.")
    })
    @DeleteMapping(value = "")
    public ResponseEntity<String> deleteResource(
            @Parameter(name = "roomId", description = "게임방의 ID", required = true, example = "3")
            @RequestParam(name = "roomId") Long roomId,

            @Parameter(name = "resourceId", description = "등록된 리소스의 ID", required = true, example = "5")
            @RequestParam(name = "resourceId") Long resourceId,

            HttpServletRequest request) {
        gameService.deleteResource(roomId, resourceId, request);
        return ResponseEntity.status(HttpStatus.OK).body("해당 리소스를 삭제했습니다.");
    }
}

