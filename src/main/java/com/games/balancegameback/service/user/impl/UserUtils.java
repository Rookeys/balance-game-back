package com.games.balancegameback.service.user.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.BadRequestException;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.core.exception.impl.UnAuthorizedException;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.domain.user.enums.LoginType;
import com.games.balancegameback.domain.user.enums.UrlType;
import com.games.balancegameback.dto.user.LoginResponse;
import com.games.balancegameback.dto.user.TokenResponse;
import com.games.balancegameback.infra.repository.redis.RedisRepository;
import com.games.balancegameback.service.jwt.JwtTokenProvider;
import com.games.balancegameback.service.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserUtils {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RedisRepository redisRepository;
    private final RestTemplate restTemplate;

    @Value("${jwt.accessTokenExpiration}")
    private long accessTokenValidTime;

    @Value("${jwt.refreshTokenExpiration}")
    private long refreshTokenValidTime;

    public Users findUserByToken(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveAccessToken(request);
        return token == null ? null : userRepository.findByEmail(jwtTokenProvider.extractEmail(token))
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));
    }

    /**
     *   AccessToken & RefreshToken 생성 및 바디 삽입,
     *   유저 정보 바디 삽입.
     */
    public LoginResponse createToken(Users users, String fileUrl) {
        String accessToken = jwtTokenProvider.createAccessToken(users.getEmail(), users.getUserRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(users.getEmail(), users.getUserRole());

        redisRepository.setValues(refreshToken, users.getEmail());
        TokenResponse tokenResponse = this.getTokenValidTime(accessToken, refreshToken);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresAt(tokenResponse.getAccessTokenExpiresAt())
                .refreshTokenExpiresAt(tokenResponse.getRefreshTokenExpiresAt())
                .email(users.getEmail())
                .nickname(users.getNickname())
                .fileUrl(fileUrl)
                .build();
    }

    /**
     * 로그인 및 회원 가입 요청 시 액세스 토큰과 정보가 유효한 지 확인함.
     */
    public void validateToken(String accessToken, LoginType loginType) {
        String url;

        switch (loginType) {
            case KAKAO -> url = UrlType.KAKAO.getTitle();
            case GOOGLE -> url = UrlType.GOOGLE.getTitle();
            default -> throw new UnAuthorizedException("잘못된 접근입니다.", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();

        if (loginType == LoginType.GOOGLE) {
            url += "?access_token=" + accessToken; // 구글의 경우 쿼리 파라미터로 토큰 전달
        } else {
            headers.setBearerAuth(accessToken);
        }

        HttpEntity<Void> request = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, request, String.class);

            // 응답 검증
            if (!response.getStatusCode().is2xxSuccessful()) {
                throw new BadRequestException("토큰이 유효하지 않습니다.", ErrorCode.RUNTIME_EXCEPTION);
            }
        } catch (Exception e) {
            log.error("Error!! + {}", e.getMessage());
            throw new BadRequestException("토큰 검증 중 문제가 발생했습니다: " + e.getMessage(), ErrorCode.RUNTIME_EXCEPTION);
        }
    }

    /**
    *   AccessToken & RefreshToken 제한 시간 출력.
    */
    public TokenResponse getTokenValidTime(String accessToken, String refreshToken) {
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTokenExpiresAt(accessTokenValidTime / 1000)
                .refreshTokenExpiresAt(refreshTokenValidTime / 1000)
                .build();
    }


}
