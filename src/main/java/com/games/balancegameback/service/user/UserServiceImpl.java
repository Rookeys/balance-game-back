package com.games.balancegameback.service.user;

import com.games.balancegameback.dto.user.LoginRequest;
import com.games.balancegameback.dto.user.SignUpRequest;
import com.games.balancegameback.dto.user.UserRequest;
import com.games.balancegameback.dto.user.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@Builder
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public void login(LoginRequest loginRequest, HttpServletResponse response) {

    }

    @Override
    public void signUp(SignUpRequest signUpRequest, HttpServletResponse response) {

    }

    @Override
    public UserResponse getProfile(HttpServletRequest request) {
        return null;
    }

    @Override
    public void updateProfile(UserRequest userRequest, HttpServletRequest request) {

    }

    @Override
    public void logout(HttpServletRequest request) {

    }

    @Override
    public void resign(HttpServletRequest request) {

    }
}
