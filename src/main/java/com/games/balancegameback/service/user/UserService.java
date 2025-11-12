package com.games.balancegameback.service.user;

import com.games.balancegameback.dto.user.*;
import com.games.balancegameback.infra.repository.user.SchedulerRepository;
import com.games.balancegameback.service.user.impl.AuthService;
import com.games.balancegameback.service.user.impl.FollowService;
import com.games.balancegameback.service.user.impl.UserProfileService;
import com.games.balancegameback.service.user.impl.UserManagementService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AuthService authService;
    private final UserManagementService userManagementService;
    private final UserProfileService userProfileService;
    private final FollowService followService;
    private final SchedulerRepository schedulerRepository;

    // ==================== 인증 관련 ====================

    // 카카오 로그인(서버 처리)
    public LoginResponse kakaoLogin(KakaoRequest kakaoRequest, HttpServletRequest request) {
        return authService.kakaoLogin(kakaoRequest, request);
    }

    // 로그인 (next-auth 방식)
    public LoginResponse login(LoginRequest loginRequest) {
        return authService.login(loginRequest);
    }

    // 회원 가입
    public LoginResponse signUp(SignUpRequest signUpRequest) {
        return userManagementService.signUp(signUpRequest);
    }

    // 테스트용 로그인
    public LoginResponse testLogin() {
        return authService.testLogin();
    }

    // 로그 아웃
    public void logout(HttpServletRequest request) {
        authService.logout(request);
    }

    // ==================== 프로필 관련 ====================

    // 이름 중복 확인
    public boolean existsByNickname(String nickname) {
        return userManagementService.existsByNickname(nickname);
    }

    // 프로필 조회
    public UserResponse getProfile(HttpServletRequest request) {
        return userProfileService.getProfile(request);
    }

    // 이메일로 다른 사용자 프로필 조회
    public UserResponse getProfileByEmail(String email, HttpServletRequest request) {
        return userProfileService.getProfileByEmail(email, request);
    }

    // 프로필 업데이트
    public void updateProfile(UserRequest userRequest, HttpServletRequest request) {
        userProfileService.updateProfile(userRequest, request);
    }

    // ==================== 회원 탈퇴 관련 ====================

    // 회원 탈퇴
    public void resign(HttpServletRequest request) {
        userManagementService.resign(request);
    }

    // 회원 탈퇴 - 즉시 삭제
    public void remove(HttpServletRequest request) {
        schedulerRepository.deleteOldDeletedUsers();
    }

    // 회원 탈퇴 - 스케쥴러 작동
    public void deleteDeactivatedUsers() {
        schedulerRepository.deleteOldDeletedUsers();
    }

    // 토큰 재발급
    public TokenResponse refresh(HttpServletRequest request) {
        return authService.refresh(request);
    }

    // ==================== 팔로우 관련 ====================

    // 팔로워 등록
    public void addFollow(String followingEmail, HttpServletRequest request) {
        followService.addFollow(followingEmail, request);
    }

    // 팔로워 목록 조회
    public List<FollowUserResponse> getFollowers(String email, HttpServletRequest request) {
        return followService.getFollowers(email, request);
    }

    // 팔로잉 목록 조회
    public List<FollowUserResponse> getFollowings(String email, HttpServletRequest request) {
        return followService.getFollowings(email, request);
    }

    // 팔로워 취소
    public void removeFollower(String followerEmail, HttpServletRequest request) {
        followService.removeFollower(followerEmail, request);
    }

    // 팔로잉 취소
    public void removeFollowing(String followingEmail, HttpServletRequest request) {
        followService.removeFollowing(followingEmail, request);
    }

    // 팔로우 여부 확인
    public boolean isFollowing(String followingEmail, HttpServletRequest request) {
        return followService.isFollowing(followingEmail, request);
    }

    // 팔로워 / 팔로잉 수 조회
    public FollowCountResponse getFollowCounts(String email) {
        return followService.getFollowCounts(email);
    }
}

