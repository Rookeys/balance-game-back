package com.games.balancegameback.service.user.impl;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.UnAuthorizedException;
import com.games.balancegameback.domain.media.Images;
import com.games.balancegameback.domain.media.enums.MediaType;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.domain.user.enums.LoginType;
import com.games.balancegameback.domain.user.enums.UserRole;
import com.games.balancegameback.dto.user.*;
import com.games.balancegameback.infra.repository.redis.RedisRepository;
import com.games.balancegameback.service.jwt.JwtTokenProvider;
import com.games.balancegameback.service.media.repository.ImageRepository;
import com.games.balancegameback.service.user.UserRepository;
import com.games.balancegameback.service.user.impl.oauth.KakaoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ImageRepository imageRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisRepository redisRepository;
    private final UserUtils userUtils;
    private final KakaoService kakaoService;

    public LoginResponse kakaoLogin(KakaoRequest kakaoRequest, HttpServletRequest request) {
        String accessToken = kakaoService.getAccessToken(kakaoRequest.getAuthorizeCode(), request);
        KakaoResponse response = kakaoService.getUserInfo(accessToken);

        Optional<Users> users = userRepository.findByUserEmail(response.getEmail());

        if (users.isEmpty()) {  // 회원 가입으로 유도
            Users user = Users.builder()
                    .nickname(this.createUniqueNickname())
                    .email(response.getEmail())
                    .loginType(LoginType.KAKAO)
                    .userRole(UserRole.USER)
                    .build();

            user = userRepository.save(user);

            if (response.getProfileImage() != null) {
                Images images = Images.builder()
                        .mediaType(MediaType.IMAGE)
                        .fileUrl(response.getProfileImage())
                        .users(user)
                        .build();

                imageRepository.save(images);
            }

            return userUtils.createToken(user, response.getProfileImage());
        }

        if (!users.get().getLoginType().equals(LoginType.KAKAO)) {
            throw new UnAuthorizedException("다른 소셜 플랫폼 가입 유저입니다.", ErrorCode.NOT_ALLOW_OTHER_FORMATS);
        }

        if (users.get().isDeleted()) {
            throw new UnAuthorizedException("회원 탈퇴한 유저입니다.", ErrorCode.NOT_ALLOW_RESIGN_EXCEPTION);
        }

        Images images = imageRepository.findByUsers(users.get());

        return userUtils.createToken(users.get(), images != null ? images.getFileUrl() : null);
    }

    public LoginResponse login(LoginRequest loginRequest) {
        userUtils.validateToken(loginRequest.getAccessToken(), loginRequest.getLoginType());
        Users users = userRepository.findByEmail(loginRequest.getEmail());

        if (users.isDeleted()) {
            throw new UnAuthorizedException("회원 탈퇴한 유저입니다.", ErrorCode.NOT_ALLOW_RESIGN_EXCEPTION);
        }

        Images images = imageRepository.findByUsers(users);

        return userUtils.createToken(users, images != null ? images.getFileUrl() : null);
    }

    public LoginResponse testLogin() {
        Users users = userRepository.findByEmail("test@test.com");
        return userUtils.createToken(users, null);
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

    private String createUniqueNickname() {
        // 형용사 목록
        String[] adjectives = {"노래하는", "춤추는", "달리는", "웃는", "뛰어노는", "생각하는", "그림 그리는", "게임하는"};

        // 명사 목록
        String[] nouns = {"돼지", "고양이", "강아지", "토끼", "호랑이", "사자", "여우", "불곰", "거북이"};

        Random random = new Random();

        String adjective = adjectives[random.nextInt(adjectives.length)];
        String noun = nouns[random.nextInt(nouns.length)];

        int number = random.nextInt(9000) + 1000;

        // 닉네임 조합
        return adjective + " " + noun + "@" + number;
    }
}

