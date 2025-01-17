package com.games.balancegameback.service.user.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.BadRequestException;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.core.exception.impl.UnAuthorizedException;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.domain.user.enums.LoginType;
import com.games.balancegameback.domain.user.enums.UrlType;
import com.games.balancegameback.infra.repository.redis.RedisRepository;
import com.games.balancegameback.service.jwt.JwtTokenProvider;
import com.games.balancegameback.service.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class UserUtils {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RedisRepository redisRepository;

    public Users findUserByToken(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveAccessToken(request);
        return token == null ? null : userRepository.findByEmail(jwtTokenProvider.extractEmail(token))
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));
    }

    /*
        로그인 및 회원 가입 요청 시 액세스 토큰과 정보가 유효한 지 확인함.
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
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // RestTemplate에 타임아웃 설정
        RestTemplate restTemplate = createRestTemplateWithTimeout();

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            // 응답 검증
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new BadRequestException("토큰이 유효하지 않습니다.", ErrorCode.RUNTIME_EXCEPTION);
            }
        } catch (Exception e) {
            throw new BadRequestException("토큰 검증 중 문제가 발생했습니다: " + e.getMessage(), ErrorCode.RUNTIME_EXCEPTION);
        }
    }

    /*
        RestTemplate 생성 및 타임 아웃 설정
     */
    private RestTemplate createRestTemplateWithTimeout() {
        int connectTimeout = 3000; // 연결 Timeout (밀리초)
        int readTimeout = 3000;    // 읽기 Timeout (밀리초)

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        return new RestTemplate(factory);
    }

    /*
        AccessToken & RefreshToken 생성 및 헤더 삽입.
     */
    public void createToken(Users users, HttpServletResponse response) {
        String accessToken = jwtTokenProvider.createAccessToken(users.getEmail(), users.getUserRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(users.getEmail(), users.getUserRole());

        jwtTokenProvider.setHeaderAccessToken(response, accessToken);
        jwtTokenProvider.setHeaderRefreshToken(response, refreshToken);

        redisRepository.setValues(refreshToken, users.getEmail());
    }
}
