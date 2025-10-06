package com.games.balancegameback.web.user;

import com.games.balancegameback.domain.user.Follow;
import com.games.balancegameback.dto.user.FollowRequest;
import com.games.balancegameback.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/follows")
@Tag(name = "Follow Controller", description = "팔로우 기능 관리 API")
public class FollowController {

    private final UserService userService;

    /**
     * 팔로워 등록
     */
    @Operation(summary = "팔로워 등록 API", description = "특정 사용자를 팔로우합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "팔로우 등록 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검사 실패 또는 이미 팔로우 중"),
            @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "해당 사용자는 존재하지 않습니다.")
    })
    @PostMapping
    public ResponseEntity<Boolean> addFollow(
            @RequestBody @Valid FollowRequest followRequest,
            HttpServletRequest request) {
        userService.addFollow(followRequest.getFollowingEmail(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(true);
    }

    /**
     * 팔로워 목록 조회
     */
    @Operation(summary = "팔로워 목록 조회 API", description = "특정 사용자의 팔로워 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "팔로워 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 사용자는 존재하지 않습니다.")
    })
    @GetMapping(value = "/followers")
    public ResponseEntity<List<Follow>> getFollowers(
            @Parameter(name = "email", description = "사용자 이메일", required = true)
            @RequestParam String email) {
        List<Follow> followers = userService.getFollowers(email);
        return ResponseEntity.ok(followers);
    }

    /**
     * 팔로잉 목록 조회
     */
    @Operation(summary = "팔로잉 목록 조회 API", description = "특정 사용자의 팔로잉 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "팔로잉 목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 사용자는 존재하지 않습니다.")
    })
    @GetMapping(value = "/followings")
    public ResponseEntity<List<Follow>> getFollowings(
            @Parameter(name = "email", description = "사용자 이메일", required = true)
            @RequestParam String email) {
        List<Follow> followings = userService.getFollowings(email);
        return ResponseEntity.ok(followings);
    }

    /**
     * 팔로워 취소
     */
    @Operation(summary = "팔로워 취소 API", description = "특정 팔로워를 제거합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "팔로워 취소 성공"),
            @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "팔로우 관계가 없습니다.")
    })
    @DeleteMapping(value = "/followers/{followerEmail}")
    public ResponseEntity<Boolean> removeFollower(
            @Parameter(name = "followerEmail", description = "제거할 팔로워의 이메일", required = true)
            @PathVariable String followerEmail,
            HttpServletRequest request) {
        userService.removeFollower(followerEmail, request);
        return ResponseEntity.ok(true);
    }

    /**
     * 팔로잉 취소
     */
    @Operation(summary = "팔로잉 취소 API", description = "특정 사용자에 대한 팔로우를 취소합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "팔로잉 취소 성공"),
            @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "팔로우 관계가 없습니다.")
    })
    @DeleteMapping(value = "/followings/{followingEmail}")
    public ResponseEntity<Boolean> removeFollowing(
            @Parameter(name = "followingEmail", description = "언팔로우할 사용자의 이메일", required = true)
            @PathVariable String followingEmail,
            HttpServletRequest request) {
        userService.removeFollowing(followingEmail, request);
        return ResponseEntity.ok(true);
    }

    /**
     * 팔로우 여부 확인
     */
    @Operation(summary = "팔로우 여부 확인 API", description = "특정 사용자를 팔로우 중인지 확인합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "팔로우 여부 조회 성공"),
            @ApiResponse(responseCode = "401", description = "로그인이 필요합니다."),
            @ApiResponse(responseCode = "404", description = "해당 사용자는 존재하지 않습니다.")
    })
    @GetMapping(value = "/check")
    public ResponseEntity<Boolean> isFollowing(
            @Parameter(name = "followingEmail", description = "확인할 사용자의 이메일", required = true)
            @RequestParam String followingEmail,
            HttpServletRequest request) {
        boolean isFollowing = userService.isFollowing(followingEmail, request);
        return ResponseEntity.ok(isFollowing);
    }

    /**
     * 팔로워 수 조회
     */
    @Operation(summary = "팔로워 수 조회 API", description = "특정 사용자의 팔로워 수를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "팔로워 수 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 사용자는 존재하지 않습니다.")
    })
    @GetMapping(value = "/followers/count")
    public ResponseEntity<Long> getFollowerCount(
            @Parameter(name = "email", description = "사용자 이메일", required = true)
            @RequestParam String email) {
        long count = userService.getFollowerCount(email);
        return ResponseEntity.ok(count);
    }

    /**
     * 팔로잉 수 조회
     */
    @Operation(summary = "팔로잉 수 조회 API", description = "특정 사용자의 팔로잉 수를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "팔로잉 수 조회 성공"),
            @ApiResponse(responseCode = "404", description = "해당 사용자는 존재하지 않습니다.")
    })
    @GetMapping(value = "/followings/count")
    public ResponseEntity<Long> getFollowingCount(
            @Parameter(name = "email", description = "사용자 이메일", required = true)
            @RequestParam String email) {
        long count = userService.getFollowingCount(email);
        return ResponseEntity.ok(count);
    }
}