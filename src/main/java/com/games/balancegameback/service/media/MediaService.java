package com.games.balancegameback.service.media;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.UnAuthorizedException;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.media.ImageRequest;
import com.games.balancegameback.dto.media.LinkRequest;
import com.games.balancegameback.dto.media.PresignedUrlRequest;
import com.games.balancegameback.dto.media.PresignedUrlsRequest;
import com.games.balancegameback.service.game.repository.GameRepository;
import com.games.balancegameback.service.media.impl.ImageService;
import com.games.balancegameback.service.media.impl.LinkService;
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
    private final ImageService imageService;
    private final LinkService linkService;
    private final GameRepository gameRepository;
    private final UserUtils userUtils;

    // Presigned URL 발급 (단일, 유저)
    public String getPreSignedUrl(PresignedUrlRequest request) {
        return presignedUrlService.getPreSignedUrl(request.getPrefix());
    }

    // Presigned URL 발급 (다중)
    public List<String> getPreSignedUrls(Long roomId, PresignedUrlsRequest urlRequest, HttpServletRequest request) {
        this.validateRequest(roomId, request);
        return presignedUrlService.getPreSignedUrls(urlRequest.getPrefix(), urlRequest.getLength());
    }

    // 이미지 저장 및 게임 리소스 추가
    public void saveImage(Long roomId, ImageRequest imageRequest, HttpServletRequest request) {
        this.validateRequest(roomId, request);
        imageService.saveImage(roomId, imageRequest);
    }

    // 링크 저장 및 게임 리소스 추가
    public void saveLink(Long roomId, LinkRequest linkRequest, HttpServletRequest request) {
        this.validateRequest(roomId, request);
        linkService.saveLink(roomId, linkRequest);
    }

    // 발급 요청한 사람이 해당 게임방 주인이 맞는지 확인.
    private void validateRequest(Long roomId, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);

        if (users == null) {
            throw new UnAuthorizedException("유효하지 않은 사용자입니다.", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }

        if (!gameRepository.existsByIdAndUsers(roomId, users)) {
            throw new UnAuthorizedException("정보가 일치하지 않습니다.", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }
    }
}
