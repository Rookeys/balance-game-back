package com.games.balancegameback.service.user.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.user.LoginResponse;
import com.games.balancegameback.dto.user.TokenResponse;
import com.games.balancegameback.infra.repository.redis.RedisRepository;
import com.games.balancegameback.service.jwt.JwtTokenProvider;
import com.games.balancegameback.service.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Log4j2
@Service
@RequiredArgsConstructor
public class UserUtils {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RedisRepository redisRepository;

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
    *   AccessToken & RefreshToken 제한 시간 출력.
    */
    public TokenResponse getTokenValidTime(String accessToken, String refreshToken) {
        return TokenResponse.builder()
                .accessToken("Bearer " + accessToken)
                .refreshToken("Bearer " + refreshToken)
                .accessTokenExpiresAt(accessTokenValidTime / 1000)
                .refreshTokenExpiresAt(refreshTokenValidTime / 1000)
                .build();
    }


}
