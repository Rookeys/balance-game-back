package com.games.balancegameback.web.user;

import com.games.balancegameback.dto.user.KakaoRequest;
import com.games.balancegameback.dto.user.LoginRequest;
import com.games.balancegameback.dto.user.LoginResponse;
import com.games.balancegameback.dto.user.TokenResponse;
import com.games.balancegameback.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/users")
@Tag(name = "OAuth Controller", description = "OAuth API")
public class AuthController {

    private final UserService userService;

    @Operation(summary = "카카오 로그인(서버 처리) API", description = "사용자의 로그인 정보를 받아 토큰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검사 실패, 조건에 맞지 않는 값이 발견됨"),
            @ApiResponse(responseCode = "401", description = "401_2 : 회원 탈퇴한 유저"),
            @ApiResponse(responseCode = "401", description = "401_3 : 다른 소셜 플랫폼 기반 가입자")
    })
    @PostMapping(value = "/login/kakao")
    public LoginResponse kakaoLogin(@RequestBody @Valid KakaoRequest kakaoRequest,
                               HttpServletRequest request) {
        return userService.kakaoLogin(kakaoRequest, request);
    }

    @Operation(summary = "로그인(Next-Auth) API", description = "사용자의 로그인 정보를 받아 토큰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검사 실패, 조건에 맞지 않는 값이 발견됨"),
            @ApiResponse(responseCode = "401", description = "401 : 존재하지 않는 유저"),
            @ApiResponse(responseCode = "401", description = "401_2 : 회원 탈퇴한 유저")
    })
    @PostMapping(value = "/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest loginRequest) {
        return userService.login(loginRequest);
    }

    @Operation(summary = "로그 아웃 API", description = "사용자를 로그 아웃 시킵니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그 아웃 성공")
    })
    @PostMapping(value = "/logout")
    public ResponseEntity<Boolean> logout(HttpServletRequest request) {
        userService.logout(request);
        return ResponseEntity.ok(Boolean.TRUE);
    }

    @Operation(summary = "토큰 재발급 API", description = "RefreshToken 으로 AccessToken 을 재발급합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "재발급 성공")
    })
    @PostMapping(value = "/refresh")
    public TokenResponse refresh(HttpServletRequest request) {
        return userService.refresh(request);
    }

    @Operation(summary = "테스트 전용 로그인 API", description = "토큰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검사 실패, 조건에 맞지 않는 값이 발견됨"),
            @ApiResponse(responseCode = "401", description = "401 : 존재하지 않는 유저"),
            @ApiResponse(responseCode = "401", description = "401_2 : 회원 탈퇴한 유저")
    })
    @PostMapping(value = "/test/login")
    public LoginResponse testLogin() {
        return userService.testLogin();
    }
}

