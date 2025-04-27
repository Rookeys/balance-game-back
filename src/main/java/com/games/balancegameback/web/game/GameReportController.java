package com.games.balancegameback.web.game;

import com.games.balancegameback.dto.game.report.GameCommentReportRequest;
import com.games.balancegameback.dto.game.report.GameReportRequest;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/games")
@Tag(name = "Game Report Controller", description = "Game Report's API")
public class GameReportController {

    private final GameService gameService;

    @Operation(summary = "게임방 신고 API", description = "정책에 맞지 않는 게임방을 신고함.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "신고 완료"),
            @ApiResponse(responseCode = "400", description = "필수 정보값이 누락됨."),
            @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "해당 게임방은 존재하지 않음.")
    })
    @PostMapping(value = "/{gameId}/report")
    public ResponseEntity<Boolean> submitGamesReport(
            @Parameter(name = "gameId", description = "게임방의 ID", required = true)
            @PathVariable(name = "gameId") Long gameId,

            @RequestBody @Valid GameReportRequest gameReportRequest,

            HttpServletRequest request) {

        gameService.submitGamesReport(gameId, gameReportRequest, request);
        return ResponseEntity.status(HttpStatus.OK).body(true);
    }

    @Operation(summary = "리소스 신고 API", description = "정책에 맞지 않는 리소스를 신고함.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "신고 완료"),
            @ApiResponse(responseCode = "400", description = "필수 정보값이 누락됨."),
            @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "해당 리소스는 존재하지 않음.")
    })
    @PostMapping(value = "/{gameId}/resources/{resourceId}/report")
    public ResponseEntity<Boolean> submitGameResourcesReport(
            @Parameter(name = "gameId", description = "게임방의 ID", required = true)
            @PathVariable(name = "gameId") Long gameId,

            @Parameter(name = "resourceId", description = "리소스 ID", required = true)
            @PathVariable(name = "resourceId") Long resourceId,

            @RequestBody @Valid GameReportRequest gameReportRequest,

            HttpServletRequest request) {

        gameService.submitGameResourcesReport(gameId, resourceId, gameReportRequest, request);
        return ResponseEntity.status(HttpStatus.OK).body(true);
    }

    @Operation(summary = "댓글 신고 API", description = "정책에 맞지 않는 댓글을 신고함.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "신고 완료"),
            @ApiResponse(responseCode = "400", description = "필수 정보값이 누락됨."),
            @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "해당 댓글은 존재하지 않음.")
    })
    @PostMapping(value = "/{gameId}/comments/report")
    public ResponseEntity<Boolean> submitGameCommentsReport(
            @Parameter(name = "gameId", description = "게임방의 ID", required = true)
            @PathVariable(name = "gameId") Long gameId,

            @RequestBody @Valid GameCommentReportRequest commentReportRequest,

            HttpServletRequest request) {

        gameService.submitGameCommentsReport(gameId, commentReportRequest, request);
        return ResponseEntity.status(HttpStatus.OK).body(true);
    }
}
