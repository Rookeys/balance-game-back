package com.games.balancegameback.service.media;

import com.games.balancegameback.dto.media.PresignedUrlRequest;
import com.games.balancegameback.dto.media.PresignedUrlsRequest;
import com.games.balancegameback.service.media.impl.PresignedUrlService;
import com.games.balancegameback.service.user.impl.UserUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final PresignedUrlService presignedUrlService;
    private final UserUtils userUtils;

    // Presigned URL 발급 (단일, 유저)
    public String getPreSignedUrl(PresignedUrlRequest request) {
        return presignedUrlService.getPreSignedUrl(request.getPrefix());
    }

    // Presigned URL 발급 (다중)
    public List<String> getPreSignedUrls(Long roomId, String token, PresignedUrlsRequest request) {
        // game 로직 완성 후 validate 로직 추가 예정.
        return presignedUrlService.getPreSignedUrls(request.getPrefix(), request.getLength());
    }
}
