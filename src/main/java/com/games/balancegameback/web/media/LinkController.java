package com.games.balancegameback.web.media;

import com.games.balancegameback.dto.media.LinkRequest;
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
@Tag(name = "Link Controller", description = "Link CRUD API")
public class LinkController {

    private final MediaService mediaService;

    @Operation(summary = "유튜브 링크 저장 API", description = "유튜브 URL과 시작, 끝 초를 저장함.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "저장 성공")
    })
    @PostMapping(value = "/resources/link")
    public ResponseEntity<Void> saveLink(@RequestParam Long roomId,
                                         @RequestBody @Valid LinkRequest linkRequest,
                                         HttpServletRequest request) {
        mediaService.saveLink(roomId, linkRequest, request);
        return ResponseEntity.status(201).build();
    }
}
