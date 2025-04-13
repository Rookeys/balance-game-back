package com.games.balancegameback.web.game;

import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.enums.CommentSortType;
import com.games.balancegameback.dto.game.comment.*;
import com.games.balancegameback.service.game.GameService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/games")
@Tag(name = "Game Resource Comments Controller", description = "Game Resource Comments CRUD API")
public class GameResourceCommentController {

    private final GameService gameService;

    @Operation(summary = "게임 리소스 부모 댓글 리스트 발급 API", description = "게임 리소스 부모 댓글 리스트 목록을 제공한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "발급 완료")
    })
    @GetMapping(value = "/{gameId}/resources/{resourceId}/comments")
    public CustomPageImpl<GameResourceParentCommentResponse> getParentCommentsByGameResource(
            @Parameter(name = "gameId", description = "게임방의 ID", required = true)
            @PathVariable(name = "gameId") Long gameId,

            @Parameter(name = "resourceId", description = "게임 리소스의 ID", required = true)
            @PathVariable(name = "resourceId") Long resourceId,

            @Parameter(name = "cursorId", description = "커서 ID (페이징 처리용)")
            @RequestParam(name = "cursorId", required = false) Long cursorId,

            @Parameter(name = "size", description = "한 페이지 당 출력 개수")
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,

            @Parameter(name = "content", description = "검색할 댓글 내용")
            @RequestParam(name = "content", required = false) String content,

            @Parameter(name = "sortType", description = "정렬 방식",
                    schema = @Schema(implementation = CommentSortType.class))
            @RequestParam(name = "sortType", required = false, defaultValue = "RECENT") CommentSortType sortType,

            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(0, size);
        GameCommentSearchRequest searchRequest = GameCommentSearchRequest.builder()
                .content(content)
                .sortType(sortType)
                .build();

        return gameService.getParentCommentsByGameResource(gameId, resourceId, cursorId, pageable, searchRequest, request);
    }

    @Operation(summary = "게임 리소스 대댓글 리스트 발급 API", description = "게임 리소스 대댓글 리스트 목록을 제공한다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "발급 완료")
    })
    @GetMapping(value = "/{gameId}/resources/{resourceId}/comments/{parentId}")
    public CustomPageImpl<GameResourceChildrenCommentResponse> getChildrenCommentsByGameResource(
            @Parameter(name = "gameId", description = "게임방의 ID", required = true)
            @PathVariable(name = "gameId") Long gameId,

            @Parameter(name = "resourceId", description = "게임 리소스의 ID", required = true)
            @PathVariable(name = "resourceId") Long resourceId,

            @Parameter(name = "parentId", description = "부모 댓글의 ID", required = true)
            @PathVariable(name = "parentId") Long parentId,

            @Parameter(name = "cursorId", description = "커서 ID (페이징 처리용)")
            @RequestParam(name = "cursorId", required = false) Long cursorId,

            @Parameter(name = "size", description = "한 페이지 당 출력 개수")
            @RequestParam(name = "size", required = false, defaultValue = "10") int size,

            @Parameter(name = "content", description = "검색할 댓글 내용")
            @RequestParam(name = "content", required = false) String content,

            @Parameter(name = "sortType", description = "정렬 방식",
                    schema = @Schema(implementation = CommentSortType.class))
            @RequestParam(name = "sortType", required = false, defaultValue = "RECENT") CommentSortType sortType,

            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(0, size);
        GameCommentSearchRequest searchRequest = GameCommentSearchRequest.builder()
                .content(content)
                .sortType(sortType)
                .build();

        return gameService.getChildrenCommentsByGameResource(gameId, resourceId, parentId,
                cursorId, pageable, searchRequest, request);
    }

    @Operation(summary = "게임 리소스 댓글 등록 API", description = "해당 리소스에 댓글을 등록할 수 있다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "댓글 등록 완료"),
            @ApiResponse(responseCode = "400", description = "댓글 내용은 필수입니다."),
            @ApiResponse(responseCode = "401", description = "로그인한 유저가 아닙니다."),
            @ApiResponse(responseCode = "404", description = "해당 리소스는 존재하지 않습니다.")
    })
    @PostMapping(value = "/{gameId}/resources/{resourceId}/comments")
    public ResponseEntity<Boolean> addResourceComment(
            @Parameter(name = "gameId", description = "게임방의 ID", required = true)
            @PathVariable(name = "gameId") Long gameId,

            @Parameter(name = "resourceId", description = "리소스의 ID", required = true)
            @PathVariable(name = "resourceId") Long resourceId,

            @RequestBody @Valid GameResourceCommentRequest commentRequest,

            HttpServletRequest request) {

        gameService.addResourceComment(gameId, resourceId, commentRequest, request);
        return ResponseEntity.status(HttpStatus.OK).body(Boolean.TRUE);
    }

    @Operation(summary = "게임 리소스 댓글 수정 API", description = "해당 댓글을 수정할 수 있다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "댓글 수정 완료"),
            @ApiResponse(responseCode = "400", description = "댓글 내용은 필수입니다."),
            @ApiResponse(responseCode = "401", description = "작성자가 아닙니다."),
            @ApiResponse(responseCode = "404", description = "해당 댓글은 존재하지 않습니다.")
    })
    @PutMapping(value = "/{gameId}/resources/{resourceId}/comments/{commentId}")
    public ResponseEntity<Boolean> updateResourceComment(
            @Parameter(name = "gameId", description = "게임방의 ID", required = true)
            @PathVariable(name = "gameId") Long gameId,

            @Parameter(name = "resourceId", description = "리소스의 ID", required = true)
            @PathVariable(name = "resourceId") Long resourceId,

            @Parameter(name = "commentId", description = "댓글의 ID", required = true)
            @PathVariable(name = "commentId") Long commentId,

            @RequestBody @Valid GameResourceCommentUpdateRequest commentRequest,

            HttpServletRequest request) {

        gameService.updateResourceComment(gameId, resourceId, commentId, commentRequest, request);
        return ResponseEntity.status(HttpStatus.OK).body(Boolean.TRUE);
    }

    @Operation(summary = "게임 리소스 댓글 삭제 API", description = "해당 댓글을 삭제할 수 있다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "댓글 삭제 완료"),
            @ApiResponse(responseCode = "401", description = "작성자가 아닙니다."),
            @ApiResponse(responseCode = "404", description = "해당 댓글은 존재하지 않습니다.")
    })
    @DeleteMapping(value = "/{gameId}/resources/{resourceId}/comments/{commentId}")
    public ResponseEntity<Boolean> deleteResourceComment(
            @Parameter(name = "gameId", description = "게임방의 ID", required = true)
            @PathVariable(name = "gameId") Long gameId,

            @Parameter(name = "resourceId", description = "리소스의 ID", required = true)
            @PathVariable(name = "resourceId") Long resourceId,

            @Parameter(name = "commentId", description = "댓글의 ID", required = true)
            @PathVariable(name = "commentId") Long commentId,

            HttpServletRequest request) {

        gameService.deleteResourceComment(gameId, resourceId, commentId, request);
        return ResponseEntity.status(HttpStatus.OK).body(Boolean.TRUE);
    }
}
