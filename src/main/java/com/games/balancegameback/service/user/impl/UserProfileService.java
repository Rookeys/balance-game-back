package com.games.balancegameback.service.user.impl;

import com.games.balancegameback.domain.media.Images;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.user.UserRequest;
import com.games.balancegameback.dto.user.UserResponse;
import com.games.balancegameback.service.media.repository.ImageRepository;
import com.games.balancegameback.service.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

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

        if (userRequest.getNickname() != null) {
            users.setNickname(userRequest.getNickname());
            userRepository.update(users);
        }

        if (userRequest.getUrl() != null) {
            Images images = imageRepository.findByUsers(users);

            if (images != null) {   // 기존 프로필 사진이 존재할 시
                images.update(userRequest.getUrl());
                imageRepository.update(images);
                // 추후 연 끊긴 S3 내 사진 제거 로직 추가 예정.
            } else {                // 프로필 사진을 처음 등록할 시
                images = Images.builder()
                        .fileUrl(userRequest.getUrl())
                        .users(users)
                        .build();

                imageRepository.save(images);
            }
        }
    }
}

