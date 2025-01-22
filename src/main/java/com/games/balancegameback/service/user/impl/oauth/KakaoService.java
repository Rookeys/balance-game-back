package com.games.balancegameback.service.user.impl.oauth;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.UnAuthorizedException;
import com.games.balancegameback.dto.user.KakaoResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class KakaoService {

    @Value("${spring.security.oauth2.client.registration.kakao-domain.client-id}")
    private String kakaoClientId;

    @Value("${spring.security.oauth2.client.registration.kakao-domain.redirect-uri}")
    private String kakaoDomainRedirectUri;

    @Value("${spring.security.oauth2.client.registration.kakao-local.redirect-uri}")
    private String kakaoLocalRedirectUri;

    private final RestTemplate restTemplate;
    private static final String reqAccessTokenURL = "https://kauth.kakao.com/oauth/token";
    private static final String reqUserInfoURL = "https://kapi.kakao.com/v2/user/me";

    public String getAccessToken(String authorizeCode, HttpServletRequest request) {
        String accessToken;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String redirectUri = this.selectRedirectUri(request);

        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("grant_type", "authorization_code");
        parameters.add("client_id", kakaoClientId);
        parameters.add("redirect_uri", redirectUri);
        parameters.add("code", authorizeCode);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(parameters, headers);

        ResponseEntity<String> responseEntity = restTemplate.postForEntity(reqAccessTokenURL, requestEntity, String.class);

        if (responseEntity.getStatusCode() == HttpStatus.OK) {
            JSONObject jsonObject = new JSONObject(Objects.requireNonNull(responseEntity.getBody()));
            accessToken = jsonObject.getString("access_token");
        } else {
            throw new UnAuthorizedException("Failed to get access token!", ErrorCode.KAKAO_ACCESS_TOKEN_FAILED);
        }

        return accessToken;
    }

    public KakaoResponse getUserInfo(String accessToken) {
        String email;
        String profileImage = null;

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<String> responseEntity = restTemplate.exchange(reqUserInfoURL, HttpMethod.GET, request, String.class);

        if (responseEntity.getStatusCode() != HttpStatus.OK) {
            throw new UnAuthorizedException("Failed to get user info!", ErrorCode.KAKAO_USER_INFO_FAILED);
        }

        JSONObject jsonObject = new JSONObject(Objects.requireNonNull(responseEntity.getBody()));
        JSONObject kakaoAccount = jsonObject.getJSONObject("kakao_account");

        email = kakaoAccount.getString("email");

        if (kakaoAccount.has("profile")) {
            JSONObject profile = kakaoAccount.getJSONObject("profile");
            if (profile.has("profile_image_url")) {
                profileImage = profile.getString("profile_image_url");
            }
        }

        return KakaoResponse.builder()
                .email(email)
                .profileImage(profileImage)
                .build();
    }

    private String selectRedirectUri(HttpServletRequest request) {
        String originHeader = request.getHeader("Origin");

        if (originHeader.contains("balance-game.com")) {
            return kakaoDomainRedirectUri;
        } else {
            return kakaoLocalRedirectUri;
        }
    }
}
