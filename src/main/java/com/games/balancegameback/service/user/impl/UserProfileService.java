package com.games.balancegameback.service.user.impl;

import com.games.balancegameback.domain.media.Images;
import com.games.balancegameback.domain.media.enums.MediaType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.user.UserRequest;
import com.games.balancegameback.dto.user.UserResponse;
import com.games.balancegameback.service.media.impl.S3Service;
import com.games.balancegameback.service.media.repository.ImageRepository;
import com.games.balancegameback.service.user.UserRepository;
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

        // 프로필 이미지 삭제 요청 (url 이 비어 있음)
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

        // 기존 이미지와 동일한 경우 닉네임만 수정.
        if (images.getFileUrl().equals(newUrl)) {
            log.info("닉네임만 수정됨.");
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
            log.warn("잘못된 URL 형식: {}", url);
            return false;
        }
    }
}

