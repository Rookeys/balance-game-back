package com.games.balancegameback.web.media;

import com.games.balancegameback.dto.media.ImageRequest;
import com.games.balancegameback.service.media.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/media")
@Tag(name = "Image Controller", description = "Image CRUD API")
public class ImageController {

    private final MediaService mediaService;

    @Operation(summary = "유저 프로필 사진 저장 API", description = "S3에 업로드한 URL을 저장함.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "저장 성공")
    })
    @PostMapping(value = "/profile")
    public ResponseEntity<Void> saveImageForUser(@RequestBody @Valid ImageRequest imageRequest,
                                                 HttpServletRequest request) {
        mediaService.saveImageForUser(imageRequest, request);
        return ResponseEntity.status(201).build();
    }


    @Operation(summary = "게임 리소스 사진 저장 API", description = "S3에 업로드한 URL을 차례대로 저장함.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "저장 성공")
    })
    @PostMapping(value = "/resources/image")
    public ResponseEntity<Void> saveImageForGame(@RequestParam Long roomId,
                                                 @RequestBody @Valid ImageRequest imageRequest,
                                                 HttpServletRequest request) {
        mediaService.saveImageForGame(roomId, imageRequest, request);
        return ResponseEntity.status(201).build();
    }
}
