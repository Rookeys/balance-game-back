package com.games.balancegameback.web.game;

import com.games.balancegameback.dto.game.GameCommentLikeRequest;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/games")
@Tag(name = "Game Comment Likes Controller", description = "Game Comment Like's API")
public class GameCommentLikeController {

    private final GameService gameService;

    @Operation(summary = "댓글 좋아요 처리 API", description = "해당 댓글의 좋아요를 올리거나 취소한다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "처리 완료"),
            @ApiResponse(responseCode = "404", description = "일치하는 데이터가 없습니다.")
    })
    @PostMapping(value = "/comments/{commentId}/likes")
    public ResponseEntity<Boolean> toggleLike(
            @Parameter(name = "commentId", description = "댓글 ID", required = true)
            @PathVariable(name = "commentId") Long commentId,

            @RequestBody @Valid GameCommentLikeRequest likeRequest,

            HttpServletRequest request) {

        gameService.toggleLike(commentId, likeRequest.isExistsLiked(), likeRequest.getSortType(), request);
        return ResponseEntity.ok(Boolean.TRUE);
    }
}
