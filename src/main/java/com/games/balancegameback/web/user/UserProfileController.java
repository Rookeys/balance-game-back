package com.games.balancegameback.web.user;

import com.games.balancegameback.core.utils.CustomPageImpl;
import com.games.balancegameback.domain.game.enums.GameSortType;
import com.games.balancegameback.dto.game.GameListResponse;
import com.games.balancegameback.dto.game.GameSearchRequest;
import com.games.balancegameback.dto.user.UserRequest;
import com.games.balancegameback.dto.user.UserResponse;
import com.games.balancegameback.service.game.GameService;
import com.games.balancegameback.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/api/v1/users")
@Tag(name = "User Profile Controller", description = "유저 프로필 관리 API")
public class UserProfileController {

    private final UserService userService;
    private final GameService gameService;

    @Operation(summary = "프로필 정보 출력 API", description = "프로필 정보를 출력합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "출력 성공")
    })
    @GetMapping(value = "/profile")
    public UserResponse getProfile(HttpServletRequest request) {
        return userService.getProfile(request);
    }

    @Operation(summary = "프로필 정보 수정 API", description = "프로필 정보를 수정합니다.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "유효성 검사 실패, 조건에 맞지 않는 값이 발견됨")
    })
    @PutMapping(value = "/profile")
    public ResponseEntity<String> updateProfile(
            @RequestBody @Valid UserRequest userRequest,
            HttpServletRequest request) {
        userService.updateProfile(userRequest, request);
        return ResponseEntity.ok("프로필 정보 수정 완료");
    }

    @Operation(summary = "내가 만든 게임 리스트 확인 API", description = "내가 만든 게임들을 무한 스크롤 형식으로 확인 가능.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "내가 만든 게임 리스트 발급 성공")
    })
    @GetMapping(value = "/games")
    public CustomPageImpl<GameListResponse> getMyGameList(
            @Parameter(name = "cursorId", description = "커서 ID", example = "4")
            @RequestParam(name = "cursorId", required = false) Long cursorId,

            @Parameter(name = "size", description = "한 페이지 당 출력 개수", example = "10")
            @RequestParam(name = "size", required = false, defaultValue = "15") int size,

            @Parameter(name = "title", description = "검색할 리소스 제목", example = "포메")
            @RequestParam(name = "title", required = false) String title,

            @Parameter(name = "sortType", description = "정렬 방식",
                    example = "idDesc",
                    schema = @Schema(allowableValues = {"idAsc", "idDesc"}))
            @RequestParam(name = "sortType", required = false, defaultValue = "idDesc") GameSortType sortType,

            HttpServletRequest request) {

        Pageable pageable = PageRequest.of(0, size);
        GameSearchRequest searchRequest = GameSearchRequest.builder()
                .title(title)
                .sortType(sortType)
                .build();

        return gameService.getMyGameList(pageable, cursorId, searchRequest, request);
    }
}

