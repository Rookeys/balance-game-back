package com.games.balancegameback.web.game;

import com.games.balancegameback.core.utils.CustomBasedPageImpl;
import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.enums.GameResourceSortType;
import com.games.balancegameback.dto.game.GameResourceDeleteRequest;
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
import jakarta.validation.constraints.Min;
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

    @Operation(summary = "특정 게임 리소스 데이터 발급 API", description = "해당 리소스의 데이터를 제공한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "발급 완료")
    })
    @GetMapping(value = "/{gameId}/resources/{resourceId}")
    public GameResourceResponse getResource(
            @Parameter(name = "gameId", description = "게임방의 ID", required = true)
            @PathVariable(name = "gameId") Long gameId,

            @Parameter(name = "resourceId", description = "리소스 ID", required = true)
            @PathVariable(name = "resourceId") Long resourceId) {

        return gameService.getResource(gameId, resourceId);
    }


    @Operation(summary = "게임방 내 리소스 총 갯수 반환 API", description = "해당 게임방의 리소스 총 갯수를 반환함.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리소스 총 갯수 반환 성공")
    })
    @GetMapping(value = "/{gameId}/resources/count")
    public Integer getCountResourcesInGames(
            @Parameter(name = "gameId", description = "게임방의 ID", required = true)
            @PathVariable(name = "gameId") Long gameId) {

        return gameService.getCountResourcesInGames(gameId);
    }


    @Operation(summary = "게임 리소스 리스트 발급 API (CursorId)", description = "해당 게임방의 리소스 목록을 제공한다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "발급 완료"),
            @ApiResponse(responseCode = "401", description = "게임룸 호스트가 아닙니다.")
    })
    @GetMapping(value = "/{gameId}/resources")
    public CustomPageImpl<GameResourceResponse> getResourcesUsingCursorId(
            @Parameter(name = "gameId", description = "게임방의 ID", required = true)
            @PathVariable(name = "gameId") Long gameId,

            @Parameter(name = "cursorId", description = "커서 ID (페이징 처리용)")
            @RequestParam(name = "cursorId", required = false) Long cursorId,

            @Parameter(name = "size", description = "한 페이지 당 출력 개수")
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,

            @Parameter(name = "title", description = "검색할 리소스 제목")
            @RequestParam(name = "title", required = false) String title,

            @Parameter(name = "sortType", description = "정렬 방식",
                    schema = @Schema(implementation = GameResourceSortType.class))
            @RequestParam(name = "sortType", required = false, defaultValue = "RECENT") GameResourceSortType sortType,

            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(0, size);
        GameResourceSearchRequest searchRequest = GameResourceSearchRequest.builder()
                .title(title)
                .sortType(sortType)
                .build();

        return gameService.getResources(gameId, cursorId, pageable, searchRequest, request);
    }

    @Operation(summary = "게임 리소스 리스트 발급 API (Page)", description = "해당 게임방의 리소스 목록을 제공한다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "발급 완료"),
            @ApiResponse(responseCode = "401", description = "게임룸 호스트가 아닙니다.")
    })
    @GetMapping(value = "/{gameId}/resources/page")
    public CustomBasedPageImpl<GameResourceResponse> getResourcesUsingPage(
            @Parameter(name = "gameId", description = "게임방의 ID", required = true)
            @PathVariable(name = "gameId") Long gameId,

            @Parameter(name = "page", description = "페이지 넘버")
            @RequestParam(name = "page", defaultValue = "1")
            @Min(value = 1, message = "페이지 번호는 1 이상이어야 합니다.") int page,

            @Parameter(name = "size", description = "한 페이지 당 출력 개수")
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,

            @Parameter(name = "title", description = "검색할 리소스 제목")
            @RequestParam(name = "title", required = false) String title,

            @Parameter(name = "sortType", description = "정렬 방식",
                    schema = @Schema(implementation = GameResourceSortType.class))
            @RequestParam(name = "sortType", required = false, defaultValue = "RECENT") GameResourceSortType sortType,

            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(page - 1, size);
        GameResourceSearchRequest searchRequest = GameResourceSearchRequest.builder()
                .title(title)
                .sortType(sortType)
                .build();

        return gameService.getResourcesUsingPage(gameId, pageable, searchRequest, request);
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
            @Parameter(name = "gameId", description = "게임방의 ID", required = true)
            @PathVariable(name = "gameId") Long gameId,

            @Parameter(name = "resourceId", description = "리소스의 ID", required = true)
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
            @Parameter(name = "gameId", description = "게임방의 ID", required = true)
            @PathVariable(name = "gameId") Long gameId,

            @Parameter(name = "resourceId", description = "리소스의 ID", required = true)
            @PathVariable(name = "resourceId") Long resourceId,

            HttpServletRequest request) {
        gameService.deleteResource(gameId, resourceId, request);
        return ResponseEntity.status(HttpStatus.OK).body(Boolean.TRUE);
    }

    @Operation(summary = "게임 리소스 선택 삭제 API", description = "등록된 리소스를 선택 삭제할 수 있다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "리소스 삭제 완료"),
            @ApiResponse(responseCode = "400", description = "resource & room ID 값은 필수입니다."),
            @ApiResponse(responseCode = "401", description = "게임룸 호스트가 아닙니다."),
            @ApiResponse(responseCode = "404", description = "해당 리소스는 없습니다.")
    })
    @DeleteMapping(value = "/{gameId}/resources")
    public ResponseEntity<Boolean> deleteSelectResources(
            @Parameter(name = "gameId", description = "게임방의 ID", required = true)
            @PathVariable(name = "gameId") Long gameId,

            @RequestBody GameResourceDeleteRequest gameResourceDeleteRequest,

            HttpServletRequest request) {
        gameService.deleteSelectResources(gameId, gameResourceDeleteRequest.getList(), request);
        return ResponseEntity.status(HttpStatus.OK).body(Boolean.TRUE);
    }
}

