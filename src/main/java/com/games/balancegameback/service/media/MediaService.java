package com.games.balancegameback.service.media;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.UnAuthorizedException;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.media.*;
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
    public List<String> getPreSignedUrls(PresignedUrlsRequest urlRequest) {
        return presignedUrlService.getPreSignedUrls(urlRequest.getPrefix(), urlRequest.getLength());
    }

    // 이미지 저장 및 게임 리소스 추가
    public void saveImage(Long gameId, ImageRequest imageRequest, HttpServletRequest request) {
        this.validateRequest(gameId, request);
        imageService.saveImage(gameId, imageRequest);
    }

    // 링크 저장 및 게임 리소스 추가
    public void saveLink(Long gameId, LinkRequest linkRequest, HttpServletRequest request) {
        this.validateRequest(gameId, request);
        linkService.saveLink(gameId, linkRequest);
    }

    // 자동으로 링크 저장 및 게임 리소스 추가
    public void autoSaveLink(Long gameId, List<AutoLinkRequest> autoLinkRequest, HttpServletRequest request) {
        this.validateRequest(gameId, request);
        linkService.autoSaveLink(gameId, autoLinkRequest);
    }

    // 발급 요청한 사람이 해당 게임방 주인이 맞는지 확인.
    private void validateRequest(Long gameId, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);

        if (users == null) {
            throw new UnAuthorizedException("유효하지 않은 사용자입니다.", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }

        if (!gameRepository.existsIdAndUsers(gameId, users)) {
            throw new UnAuthorizedException("정보가 일치하지 않습니다.", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }
    }
}
