package com.games.balancegameback.web.media;

import com.games.balancegameback.dto.media.PresignedUrlRequest;
import com.games.balancegameback.service.media.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/media")
@Tag(name = "Presigned URL Controller", description = "Media Upload & Update API")
public class PresignedUrlController {

    private final MediaService mediaService;

    @Operation(summary = "업로드 API", description = "AWS S3 저장소에 업로드할 수 있는 URL 발급")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL 발급 성공")
    })
    @PostMapping(value = "/upload")
    public String getPreSignedUrl(@RequestBody PresignedUrlRequest request) {
        return mediaService.getPreSignedUrl(request);
    }
}
