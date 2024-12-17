package com.games.balancegameback.service.user.impl;

import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.user.UserRequest;
import com.games.balancegameback.dto.user.UserResponse;
import com.games.balancegameback.service.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserUtils userUtils;

    public UserResponse getProfile(HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);

        return UserResponse.builder()
                .nickname(users.getNickname())
                .email(users.getEmail())
                .fileUrl("추가 예정")   // Media 로직 완성 후 추가 예정.
                .build();
    }

    @Transactional
    public void updateProfile(UserRequest userRequest, HttpServletRequest request) {
        Users users = userUtils.findUserByToken(request);

        if (userRequest.getNickname() != null) {
            users.setNickname(userRequest.getNickname());
            userRepository.save(users);
        }

        if (userRequest.getUrl() != null) {
            // Media 로직 완성 후 추가 예정.
        }
    }
}

