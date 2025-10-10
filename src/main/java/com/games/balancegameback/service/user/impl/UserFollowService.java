package com.games.balancegameback.service.user.impl;

import com.games.balancegameback.dto.user.UserFollowRequest;
import com.games.balancegameback.service.media.repository.ImageRepository;
import com.games.balancegameback.service.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserFollowService {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final UserUtils userUtils;

    @Transactional
    public void saveFollower(UserFollowRequest followRequest, HttpServletRequest request) {

    }

    @Transactional
    public void saveFollowing(UserFollowRequest followRequest, HttpServletRequest request) {

    }


}
