package com.games.balancegameback.service.user;

import com.games.balancegameback.dto.user.LoginRequest;
import com.games.balancegameback.dto.user.SignUpRequest;
import com.games.balancegameback.dto.user.UserRequest;
import com.games.balancegameback.dto.user.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface UserService {

    void login(LoginRequest loginRequest, HttpServletResponse response);

    void signUp(SignUpRequest signUpRequest, HttpServletResponse response);

    UserResponse getProfile(HttpServletRequest request);

    void updateProfile(UserRequest userRequest, HttpServletRequest request);

    void logout(HttpServletRequest request);

    void resign(HttpServletRequest request);
}
