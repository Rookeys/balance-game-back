package com.games.balancegameback.web.media;

import com.games.balancegameback.dto.media.LinkRequest;
import com.games.balancegameback.service.media.MediaService;
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
@RequestMapping(value = "/api/v1")
@Tag(name = "Media Link Controller", description = "Link CRUD API")
public class MediaLinkController {

    private final MediaService mediaService;

    @Operation(summary = "유튜브 링크 저장 API", description = "유튜브 URL과 시작, 끝 초를 저장함.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "저장 성공")
    })
    @PostMapping(value = "/games/{gameId}/media/links")
    public ResponseEntity<Boolean> saveLink(
            @Parameter(name = "gameId", description = "게임방의 ID", required = true, example = "12345")
            @PathVariable(name = "gameId") Long gameId,

            @RequestBody @Valid LinkRequest linkRequest,
            HttpServletRequest request) {
        mediaService.saveLink(gameId, linkRequest, request);
        return ResponseEntity.status(201).body(Boolean.TRUE);
    }
}

