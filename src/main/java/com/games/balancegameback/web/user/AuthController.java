package com.games.balancegameback.web.user;

import com.games.balancegameback.dto.user.LoginRequest;
import com.games.balancegameback.dto.user.SignUpRequest;
import com.games.balancegameback.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

    @Operation(summary = "로그인 API", description = "사용자의 로그인 정보를 받아 토큰을 발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그인 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검사 실패, 조건에 맞지 않는 값이 발견됨"),
            @ApiResponse(responseCode = "401", description = "401 : 존재하지 않는 유저"),
            @ApiResponse(responseCode = "401", description = "401_2 : 회원 탈퇴한 유저")
    })
    @PostMapping(value = "/login")
    public ResponseEntity<String> login(@RequestBody @Valid LoginRequest loginRequest, HttpServletResponse response) {
        userService.login(loginRequest, response);
        return ResponseEntity.ok("로그인 성공");
    }


    @Operation(summary = "로그 아웃 API", description = "사용자를 로그 아웃 시킵니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "로그 아웃 성공")
    })
    @GetMapping(value = "/logout")
    public ResponseEntity<String> logout(HttpServletRequest request) {
        userService.logout(request);
        return ResponseEntity.ok("로그 아웃 성공");
    }


    @Operation(summary = "토큰 재발급 API", description = "RefreshToken 으로 AccessToken 을 재발급합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "재발급 성공")
    })
    @GetMapping(value = "/reissue")
    public ResponseEntity<String> reissue(HttpServletRequest request, HttpServletResponse response) {
        userService.reissue(request, response);
        return ResponseEntity.ok("토큰 재발급 성공");
    }
}
