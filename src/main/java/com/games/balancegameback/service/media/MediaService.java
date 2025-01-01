package com.games.balancegameback.service.media;

import com.games.balancegameback.dto.media.PresignedUrlRequest;
import com.games.balancegameback.dto.media.PresignedUrlsRequest;
import com.games.balancegameback.service.media.impl.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final ImageService imageService;

    // Presigned URL 발급 (단일, 유저)
    public String getPreSignedUrl(PresignedUrlRequest request) {
        return imageService.getPreSignedUrl(null, request.getPrefix());
    }

    // Presigned URL 발급 (다중)
    public List<String> getPreSignedUrls(Long roomId, PresignedUrlsRequest request) {
        return imageService.getPreSignedUrls(roomId, request.getPrefix(), request.getLength());
    }

    // S3 내 객체와 DB 내 정보 validate
    public boolean validateUploadedFile(Long roomId, String imageUrl) {
        return imageService.isFileExistOnS3(roomId, imageUrl);
    }
}
