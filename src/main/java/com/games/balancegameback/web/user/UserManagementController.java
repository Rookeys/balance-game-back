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
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Boolean> signUp(
            @RequestBody @Valid SignUpRequest signUpRequest,
            HttpServletResponse response) {
        userService.signUp(signUpRequest, response);
        return ResponseEntity.status(HttpStatus.CREATED).body(Boolean.TRUE);
    }

    @Operation(summary = "회원 탈퇴 API", description = "회원 탈퇴 요청을 처리합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 대기 전환"),
    })
    @PostMapping(value = "/resign")
    public ResponseEntity<Boolean> resign(HttpServletRequest request) {
        userService.resign(request);
        return ResponseEntity.ok(Boolean.TRUE);
    }

    @Operation(summary = "회원 탈퇴 취소 API", description = "회원 탈퇴 요청을 취소합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "회원 탈퇴 취소 성공"),
    })
    @PostMapping(value = "/cancel/resign")
    public ResponseEntity<Boolean> cancelResign(@RequestBody String email) {
        userService.cancelResign(email);
        return ResponseEntity.ok(Boolean.TRUE);
    }

    @Operation(summary = "중복 이름 확인 API", description = "이름이 중복되었다면 True, 아니면 False 를 반환합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "확인 성공"),
    })
    @GetMapping(value = "/exists")
    public Boolean existsByNickname(
            @Parameter(name = "nickname", description = "유저 닉네임", required = true, example = "testUser")
            @RequestParam(name = "nickname") String nickname) {
        return userService.existsByNickname(nickname);
    }
}

