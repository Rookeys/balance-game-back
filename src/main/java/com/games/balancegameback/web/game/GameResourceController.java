package com.games.balancegameback.web.game;

import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.enums.GameResourceSortType;
import com.games.balancegameback.dto.game.GameResourceRequest;
import com.games.balancegameback.dto.game.GameResourceResponse;
import com.games.balancegameback.dto.game.GameResourceSearchRequest;
import com.games.balancegameback.service.game.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/games")
@Tag(name = "Game Resource Controller", description = "Game Resource CRUD API")
public class GameResourceController {

    private final GameService gameService;

    @Operation(summary = "게임 리소스 리스트 발급 API", description = "해당 게임방의 리소스 목록을 제공한다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "발급 완료"),
            @ApiResponse(responseCode = "401", description = "게임룸 호스트가 아닙니다.")
    })
    @GetMapping(value = "/{gameId}/resources")
    public CustomPageImpl<GameResourceResponse> getResources(
            @Parameter(name = "gameId", description = "게임방의 ID", required = true, example = "3")
            @PathVariable(name = "gameId") Long gameId,

            @Parameter(name = "cursorId", description = "커서 ID (페이징 처리용)", example = "15")
            @RequestParam(name = "cursorId", required = false) Long cursorId,

            @Parameter(name = "size", description = "한 페이지 당 출력 개수", example = "10")
            @RequestParam(name = "size", required = false, defaultValue = "15") int size,

            @Parameter(name = "title", description = "검색할 리소스 제목", example = "스페셜 아이템")
            @RequestParam(name = "title", required = false) String title,

            @Parameter(name = "sortType", description = "정렬 방식",
                    example = "winRateDesc",
                    schema = @Schema(allowableValues = {"winRateAsc", "winRateDesc", "idAsc", "idDesc"}))
            @RequestParam(name = "sortType", required = false, defaultValue = "idDesc") GameResourceSortType sortType,

            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(0, size);
        GameResourceSearchRequest searchRequest = GameResourceSearchRequest.builder()
                .title(title)
                .sortType(sortType)
                .build();

        return gameService.getResources(gameId, cursorId, pageable, searchRequest, request);
    }

    @Operation(summary = "게임 리소스 수정 API", description = "리소스의 제목이나 URL 등을 수정할 수 있다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리소스 정보 수정 완료"),
            @ApiResponse(responseCode = "400", description = "resource & room ID 값은 필수입니다."),
            @ApiResponse(responseCode = "401", description = "게임룸 호스트가 아닙니다."),
            @ApiResponse(responseCode = "404", description = "해당 리소스는 없습니다.")
    })
    @PutMapping(value = "/{gameId}/resources/{resourceId}")
    public ResponseEntity<Boolean> updateResource(
            @Parameter(name = "gameId", description = "게임방의 ID", required = true, example = "3")
            @PathVariable(name = "gameId") Long gameId,

            @Parameter(name = "resourceId", description = "리소스의 ID", required = true, example = "5")
            @PathVariable(name = "resourceId") Long resourceId,

            @RequestBody GameResourceRequest gameResourceRequest,
            HttpServletRequest request) {
        gameService.updateResource(gameId, resourceId, gameResourceRequest, request);
        return ResponseEntity.status(HttpStatus.OK).body(Boolean.TRUE);
    }

    @Operation(summary = "게임 리소스 삭제 API", description = "등록된 리소스를 삭제할 수 있다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리소스 삭제 완료"),
            @ApiResponse(responseCode = "400", description = "resource & room ID 값은 필수입니다."),
            @ApiResponse(responseCode = "401", description = "게임룸 호스트가 아닙니다."),
            @ApiResponse(responseCode = "404", description = "해당 리소스는 없습니다.")
    })
    @DeleteMapping(value = "/{gameId}/resources/{resourceId}")
    public ResponseEntity<Boolean> deleteResource(
            @Parameter(name = "gameId", description = "게임방의 ID", required = true, example = "3")
            @PathVariable(name = "gameId") Long gameId,

            @Parameter(name = "resourceId", description = "리소스의 ID", required = true, example = "5")
            @PathVariable(name = "resourceId") Long resourceId,

            HttpServletRequest request) {
        gameService.deleteResource(gameId, resourceId, request);
        return ResponseEntity.status(HttpStatus.OK).body(Boolean.TRUE);
    }
}

