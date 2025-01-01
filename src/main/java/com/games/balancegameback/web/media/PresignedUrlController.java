package com.games.balancegameback.web.media;

import com.games.balancegameback.dto.media.PresignedUrlRequest;
import com.games.balancegameback.dto.media.PresignedUrlsRequest;
import com.games.balancegameback.service.media.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/media")
@Tag(name = "Presigned URL Controller", description = "Media Upload & Update API")
public class PresignedUrlController {

    private final MediaService mediaService;

    @Operation(summary = "단일 업로드 API (User Profile)", description = "AWS S3 저장소에 업로드할 수 있는 단일 URL 발급")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL 발급 성공"),
            @ApiResponse(responseCode = "400", description = "prefix 값이 확인되지 않음.")
    })
    @PostMapping(value = "/single")
    public String getPreSignedUrlForUser(@RequestBody PresignedUrlRequest request) {
        return mediaService.getPreSignedUrl(request);
    }


    @Operation(summary = "다중 업로드 API", description = "AWS S3 저장소에 업로드할 수 있는 다중 URL 발급")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL 발급 성공"),
            @ApiResponse(responseCode = "400", description = "prefix 값이 확인되지 않음.")
    })
    @PostMapping(value = "/multiple")
    public List<String> getPreSignedUrl(@RequestParam(value = "roomId") Long roomId,
                                                     @RequestBody PresignedUrlsRequest request) {
        return mediaService.getPreSignedUrls(roomId, request);
    }
}
