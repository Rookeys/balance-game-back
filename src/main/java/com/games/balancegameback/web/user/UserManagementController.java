package com.games.balancegameback.web.user;

import com.games.balancegameback.dto.user.SignUpRequest;
import com.games.balancegameback.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/users")
@Tag(name = "User Management Controller", description = "유저 관리 API")
public class UserManagementController {

    private final UserService userService;

    @Operation(summary = "회원 가입 API", description = "유저의 정보를 입력받아 회원 가입을 처리합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "회원 가입 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검사 실패, 조건에 맞지 않는 값이 발견됨"),
            @ApiResponse(responseCode = "401", description = "401 : 소셜 로그인 측 토큰 불량"),
            @ApiResponse(responseCode = "401", description = "401_2 : 중복된 닉네임 또는 이메일")
    })
    @PostMapping(value = "/signup")
    public ResponseEntity<Void> signUp(
            @RequestBody @Valid SignUpRequest signUpRequest,
            HttpServletResponse response) {
        userService.signUp(signUpRequest, response);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "회원 탈퇴 API", description = "회원 탈퇴 요청을 처리합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 대기 전환"),
    })
    @PostMapping(value = "/resign")
    public ResponseEntity<String> resign(HttpServletRequest request) {
        userService.resign(request);
        return ResponseEntity.ok("회원 탈퇴 대기 전환");
    }

    @Operation(summary = "회원 탈퇴 취소 API", description = "회원 탈퇴 요청을 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 취소 성공"),
    })
    @PostMapping(value = "/cancel")
    public ResponseEntity<String> cancelResign(
            @Parameter(name = "email", description = "유저 이메일", required = true, example = "user@example.com")
            @RequestBody String email) {
        userService.cancelResign(email);
        return ResponseEntity.ok("회원 탈퇴 취소 성공");
    }
}

