package com.games.balancegameback.service.media;

import com.games.balancegameback.dto.media.PresignedUrlRequest;
import com.games.balancegameback.dto.media.PresignedUrlsRequest;
import com.games.balancegameback.service.media.impl.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaRepository mediaRepository;
    private final ImageService imageService;

    // Presigned URL 발급 (단일)
    public Map<String, String> getPreSignedUrl(PresignedUrlRequest request) {
        return imageService.getPreSignedUrl(request.getPrefix(), request.getFileName());
    }

    // Presigned URL 발급 (다중)
    public List<Map<String, String>> getPreSignedUrls(PresignedUrlsRequest request) {
        return imageService.getPreSignedUrls(request.getPrefix(), request.getFileNameList());
    }
}
