package com.games.balancegameback.service.user;

import com.games.balancegameback.dto.user.*;
import com.games.balancegameback.service.user.impl.AuthService;
import com.games.balancegameback.service.user.impl.UserProfileService;
import com.games.balancegameback.service.user.impl.UserManagementService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthService authService;
    private final UserManagementService userManagementService;
    private final UserProfileService userProfileService;

    // 로그인
    public TokenResponse login(LoginRequest loginRequest, HttpServletResponse response) {
        return authService.login(loginRequest, response);
    }

    // 테스트용 로그인
    public TokenResponse testLogin(HttpServletResponse response) {
        return authService.testLogin(response);
    }

    // 회원 가입
    public void signUp(SignUpRequest signUpRequest, HttpServletResponse response) {
        userManagementService.signUp(signUpRequest, response);
    }

    // 이름 중복 확인
    public boolean existsByNickname(String nickname) {
        return userManagementService.existsByNickname(nickname);
    }

    // 로그 아웃
    public void logout(HttpServletRequest request) {
        authService.logout(request);
    }

    // 프로필 조회
    public UserResponse getProfile(HttpServletRequest request) {
        return userProfileService.getProfile(request);
    }

    // 프로필 업데이트
    public void updateProfile(UserRequest userRequest, HttpServletRequest request) {
        userProfileService.updateProfile(userRequest, request);
    }

    // 회원 탈퇴
    public void resign(HttpServletRequest request) {
        userManagementService.resign(request);
    }

    // 탈퇴 취소
    public void cancelResign(String email) {
        userManagementService.cancelResign(email);
    }

    // 토큰 재발급
    public TokenResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        return authService.refresh(request, response);
    }
}

