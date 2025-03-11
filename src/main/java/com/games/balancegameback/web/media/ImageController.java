package com.games.balancegameback.web.media;

import com.games.balancegameback.dto.media.ImageRequest;
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
@Tag(name = "Image Controller", description = "Image CRUD API")
public class ImageController {

    private final MediaService mediaService;

    @Operation(summary = "게임 리소스 사진 저장 API", description = "S3에 업로드한 URL을 차례대로 저장함.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "저장 성공")
    })
    @PostMapping(value = "/games/{gameId}/media/images")
    public ResponseEntity<Boolean> saveImageForGame(
            @Parameter(name = "gameId", description = "게임방의 ID", required = true, example = "13")
            @PathVariable(name = "gameId") Long gameId,

            @RequestBody @Valid ImageRequest imageRequest,
            HttpServletRequest request) {
        mediaService.saveImage(gameId, imageRequest, request);
        return ResponseEntity.status(201).body(Boolean.TRUE);
    }
}

