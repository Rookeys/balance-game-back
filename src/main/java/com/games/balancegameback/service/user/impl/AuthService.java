package com.games.balancegameback.service.user.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.UnAuthorizedException;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.dto.user.LoginRequest;
import com.games.balancegameback.dto.user.TokenResponse;
import com.games.balancegameback.infra.repository.redis.RedisRepository;
import com.games.balancegameback.service.jwt.JwtTokenProvider;
import com.games.balancegameback.service.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisRepository redisRepository;
    private final UserUtils userUtils;

    public TokenResponse login(LoginRequest loginRequest) {
        userUtils.validateToken(loginRequest.getAccessToken(), loginRequest.getLoginType());
        Optional<Users> users = userRepository.findByEmail(loginRequest.getEmail());

        if (users.isEmpty()) {
            throw new UnAuthorizedException("유저를 찾을 수 없습니다.", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }

        if (users.get().isDeleted()) {
            throw new UnAuthorizedException("회원 탈퇴한 유저입니다.", ErrorCode.NOT_ALLOW_RESIGN_EXCEPTION);
        }

        return userUtils.createToken(users.get());
    }

    public TokenResponse testLogin() {
        Optional<Users> users = userRepository.findByEmail("test@test.com");
        return userUtils.createToken(users.get());
    }

    public void logout(HttpServletRequest request) {
        String refreshToken = jwtTokenProvider.resolveRefreshToken(request);
        redisRepository.delValues(refreshToken);
    }

    public TokenResponse refresh(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveRefreshToken(request);

        String accessToken = jwtTokenProvider.reissueAccessToken(token);
        String refreshToken = jwtTokenProvider.reissueRefreshToken(token);

        return userUtils.getTokenValidTime(accessToken, refreshToken);
    }
}

