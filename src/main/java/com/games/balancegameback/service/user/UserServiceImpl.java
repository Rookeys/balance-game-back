package com.games.balancegameback.service.user;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.core.exception.impl.UnAuthorizedException;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.domain.user.enums.LoginType;
import com.games.balancegameback.domain.user.enums.UrlType;
import com.games.balancegameback.dto.user.LoginRequest;
import com.games.balancegameback.dto.user.SignUpRequest;
import com.games.balancegameback.dto.user.UserRequest;
import com.games.balancegameback.dto.user.UserResponse;
import com.games.balancegameback.infra.repository.redis.RedisRepository;
import com.games.balancegameback.service.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

@Service
@Builder
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisRepository redisRepository;

    @Override
    public void login(LoginRequest loginRequest, HttpServletResponse response) {
        this.validateToken(loginRequest.getCode(), loginRequest.getLoginType());

        Optional<Users> users = userRepository.findByEmail(loginRequest.getEmail());

        if (users.isEmpty()) {
            throw new UnAuthorizedException("유저를 찾을 수 없습니다.", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }

        if (users.get().isDeleted()) {
            throw new UnAuthorizedException("회원 탈퇴한 유저입니다.", ErrorCode.NOT_ALLOW_RESIGN_EXCEPTION);
        }

        createToken(users.get(), response);
    }

    @Override
    @Transactional
    public void signUp(SignUpRequest signUpRequest, HttpServletResponse response) {
        this.validateToken(signUpRequest.getCode(), signUpRequest.getLoginType());

        Users users = signUpRequest.toDomain();
        userRepository.save(users);

        createToken(users, response);
    }

    @Override
    public UserResponse getProfile(HttpServletRequest request) {
        Users users = this.findUserByToken(request);

        return UserResponse.builder()
                .nickname(users.getNickname())
                .email(users.getEmail())
                .fileUrl("추가 예정")   // Media 로직 완성 후 추가 예정.
                .build();
    }

    @Override
    @Transactional
    public void updateProfile(UserRequest userRequest, HttpServletRequest request) {
        Users users = this.findUserByToken(request);

        if (userRequest.getNickname() != null) {
            users.setNickname(userRequest.getNickname());
        }

        if (userRequest.getUrl() != null) {
            // Media 로직 완성 후 추가 예정.
        }

        userRepository.save(users);
    }

    @Override
    public void logout(HttpServletRequest request) {
        redisRepository.delValues(jwtTokenProvider.resolveRefreshToken(request));
        jwtTokenProvider.expireToken(jwtTokenProvider.resolveAccessToken(request));
    }

    @Override
    @Transactional
    public void resign(HttpServletRequest request) {
        Users user = this.findUserByToken(request);
        user.setDeleted(true);

        userRepository.save(user);
        this.logout(request);
    }

    @Override
    @Transactional
    public void cancelResign(String email) {
        if (userRepository.existsByEmailAndDeleted(email, true)) {
            Users user = userRepository.findByEmail(email).orElseThrow(()
                    -> new NotFoundException("404", ErrorCode.NOT_FOUND_EXCEPTION));

            user.setDeleted(false);
            userRepository.save(user);
        } else {
            throw new UnAuthorizedException("회원 탈퇴한 유저입니다.", ErrorCode.NOT_ALLOW_RESIGN_EXCEPTION);
        }
    }

    @Override
    public void reissue(HttpServletRequest request, HttpServletResponse response) {
        String accessToken = jwtTokenProvider.reissueAccessToken(jwtTokenProvider.resolveAccessToken(request));
        String refreshToken = jwtTokenProvider.reissueRefreshToken(jwtTokenProvider.resolveRefreshToken(request));

        jwtTokenProvider.setHeaderAccessToken(response, accessToken);
        jwtTokenProvider.setHeaderRefreshToken(response, refreshToken);
    }

    @Override
    public Users findUserByToken(HttpServletRequest request) {
        String token = jwtTokenProvider.resolveAccessToken(request);
        return token == null ? null : userRepository.findByEmail(jwtTokenProvider.extractEmail(token))
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다.", ErrorCode.NOT_FOUND_EXCEPTION));
    }

    /*
        로그인 및 회원 가입 요청 시 액세스 토큰과 정보가 유효한 지 확인함.
     */
    private void validateToken(String code, LoginType loginType) {
        String url;

        switch (loginType) {
            case KAKAO -> url = UrlType.KAKAO.getTitle();
            case GOOGLE -> url = UrlType.GOOGLE.getTitle();
            default -> throw new UnAuthorizedException("잘못된 접근입니다.", ErrorCode.ACCESS_DENIED_EXCEPTION);
        }

        // 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + code);
        HttpEntity<Void> request = new HttpEntity<>(headers);

        // RestTemplate에 타임아웃 설정
        RestTemplate restTemplate = createRestTemplateWithTimeout();

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

            // 응답 검증
            if (!response.getStatusCode().is2xxSuccessful() || response.getBody() == null) {
                throw new UnAuthorizedException("토큰이 유효하지 않습니다.", ErrorCode.ACCESS_DENIED_EXCEPTION);
            }
        } catch (Exception e) {
            throw new UnAuthorizedException("토큰 검증 중 문제가 발생했습니다: " + e.getMessage(), ErrorCode.ACCESS_DENIED_EXCEPTION);
        }
    }

    /*
        RestTemplate 생성 및 타임 아웃 설정
     */
    private RestTemplate createRestTemplateWithTimeout() {
        int connectTimeout = 3000; // 연결 타임아웃 (밀리초)
        int readTimeout = 3000;    // 읽기 타임아웃 (밀리초)

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);

        return new RestTemplate(factory);
    }

    /*
        AccessToken & RefreshToken 생성 및 헤더 삽입.
     */
    private void createToken(Users users, HttpServletResponse response) {
        String accessToken = jwtTokenProvider.createAccessToken(users.getEmail(), users.getUserRole());
        String refreshToken = jwtTokenProvider.createRefreshToken(users.getEmail(), users.getUserRole());

        jwtTokenProvider.setHeaderAccessToken(response, accessToken);
        jwtTokenProvider.setHeaderRefreshToken(response, refreshToken);

        redisRepository.setValues(refreshToken, users.getEmail());
    }
}
