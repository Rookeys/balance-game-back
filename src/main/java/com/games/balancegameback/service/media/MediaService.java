package com.games.balancegameback.service.media;

import com.games.balancegameback.dto.media.PresignedUrlRequest;
import com.games.balancegameback.service.media.impl.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final MediaRepository mediaRepository;
    private final ImageService imageService;

    // Presigned URL 발급
    public String getPreSignedUrl(PresignedUrlRequest request) {
        return imageService.getPreSignedUrl(request.getPrefix(), request.getFileName());
    }
}
