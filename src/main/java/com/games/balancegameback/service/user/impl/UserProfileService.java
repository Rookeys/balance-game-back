package com.games.balancegameback.service.user.impl;

import com.games.balancegameback.domain.media.Images;
import com.games.balancegameback.domain.media.enums.MediaType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.user.UserRequest;
import com.games.balancegameback.dto.user.UserResponse;
import com.games.balancegameback.service.media.impl.S3Service;
import com.games.balancegameback.service.media.repository.ImageRepository;
import com.games.balancegameback.service.user.repository.FollowRepository;
import com.games.balancegameback.service.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final S3Service s3Service;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final FollowRepository followRepository;
    private final UserUtils userUtils;

    public UserResponse getProfile(HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        Images images = imageRepository.findByUsers(users);

        return UserResponse.builder()
                .nickname(users.getNickname())
                .email(users.getEmail())
                .fileUrl(images == null ? null : images.getFileUrl())
                .build();
    }

    /**
     * 이메일로 다른 사용자 프로필 조회
     * 
     * @param email 조회할 사용자 이메일
     * @return 사용자 프로필 정보
     */
    public UserResponse getProfileByEmail(String email, HttpServletRequest request) {
        Users targetUser = userRepository.findByEmail(email);
        Images images = imageRepository.findByUsers(targetUser);
        
        // 팔로우 여부 확인
        boolean isFollowing = false;
        try {
            Users currentUser = userUtils.findUserByToken(request);
            if (currentUser != null && !currentUser.getUid().equals(targetUser.getUid())) {
                isFollowing = followRepository.existsByFollowerUidAndFollowingUid(
                    currentUser.getUid(), 
                    targetUser.getUid()
                );
            }
        } catch (Exception e) {
            // 로그인하지 않은 경우
            log.debug("Failed to get current user from token", e);
        }
        
        return UserResponse.builder()
                .nickname(targetUser.getNickname())
                .email(targetUser.getEmail())
                .fileUrl(images == null ? null : images.getFileUrl())
                .isFollowing(isFollowing)
                .build();
    }

    @Transactional
    public void updateProfile(UserRequest userRequest, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);
        Images images = imageRepository.findByUsers(users);

        // 닉네임 수정
        if (userRequest.getNickname() != null) {
            users.setNickname(userRequest.getNickname());
            userRepository.update(users);
        }

        String newUrl = userRequest.getUrl();

        // 프로필 이미지 삭제 요청
        if (newUrl == null || newUrl.isEmpty()) {
            if (images != null && isValidUrl(images.getFileUrl())) {
                s3Service.deleteImageByUrl(images.getFileUrl());
                imageRepository.delete(images.getId());
            }

            return;
        }

        // 기존 이미지가 없을 경우 새로 저장.
        if (images == null) {
            Images newImage = Images.builder()
                    .mediaType(MediaType.IMAGE)
                    .users(users)
                    .fileUrl(newUrl)
                    .build();

            imageRepository.save(newImage);
            return;
        }

        // 기존 이미지와 동일한 경우
        if (images.getFileUrl().equals(newUrl)) {
            return;
        }

        // 기존 이미지와 다르면 기존 이미지 삭제 후 새 이미지 등록.
        if (isValidUrl(images.getFileUrl())) {
            s3Service.deleteImageByUrl(images.getFileUrl());
        }

        images.update(newUrl);
        imageRepository.update(images);
    }

    private boolean isValidUrl(String url) {
        try {
            if (url == null || url.isBlank()) {
                return false;
            }
            URI uri = new URI(url);
            return uri.isAbsolute();
        } catch (Exception e) {
            return false;
        }
    }
}
