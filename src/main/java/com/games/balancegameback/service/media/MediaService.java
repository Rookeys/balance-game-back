package com.games.balancegameback.service.media;

import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.media.PresignedUrlRequest;
import com.games.balancegameback.dto.media.PresignedUrlsRequest;
import com.games.balancegameback.service.game.repository.GameRepository;
import com.games.balancegameback.service.media.impl.PresignedUrlService;
import com.games.balancegameback.service.user.impl.UserUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final PresignedUrlService presignedUrlService;
    private final GameRepository gameRepository;
    private final UserUtils userUtils;

    // Presigned URL 발급 (단일, 유저)
    public String getPreSignedUrl(PresignedUrlRequest request) {
        return presignedUrlService.getPreSignedUrl(request.getPrefix());
    }

    // Presigned URL 발급 (다중)
    public List<String> getPreSignedUrls(Long roomId, PresignedUrlsRequest urlRequest, HttpServletRequest request) {
        // game 로직 완성 후 validate 로직 추가 예정.
        return presignedUrlService.getPreSignedUrls(urlRequest.getPrefix(), urlRequest.getLength());
    }

    // 발급 요청한 사람이 해당 게임방 주인이 맞는지 확인.
    private void validateRequest(Long roomId, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);


    }
}
