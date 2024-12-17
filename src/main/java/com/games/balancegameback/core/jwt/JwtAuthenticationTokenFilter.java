package com.games.balancegameback.core.jwt;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.infra.repository.redis.RedisRepository;
import com.games.balancegameback.service.jwt.JwtTokenProvider;
import io.jsonwebtoken.*;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "**/swagger", "**/v3/api-docs", "**/users/login", "**/users/signup"
    );

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisRepository redisRepository;

    @SneakyThrows
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) {
        String path = request.getRequestURI();

        // 인증이 필요 없는 요청은 스킵.
        if (skipPath(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = jwtTokenProvider.resolveAccessToken(request);
        String refreshToken = jwtTokenProvider.resolveRefreshToken(request);

        try {
            if (accessToken == null && refreshToken != null) {
                handleRefreshToken(refreshToken, path, filterChain, request, response);
                return;
            }

            if (accessToken != null && isValidAccessToken(accessToken)) {
                setAuthentication(accessToken);
            }
        } catch (JwtException e) {
            handleJwtException(e, response);
            return;
        } catch (RuntimeException e) {
            setResponse(response, ErrorCode.JWT_COMPLEX_ERROR);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean skipPath(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(path::contains);
    }

    private void handleRefreshToken(String refreshToken, String path, FilterChain filterChain,
                                    HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException, JSONException {
        if (jwtTokenProvider.validateToken(refreshToken) && redisRepository.isRefreshTokenValid(refreshToken)
                && path.contains("/reissue")) {
            filterChain.doFilter(request, response);
        } else {
            setResponse(response, ErrorCode.INVALID_TOKEN_EXCEPTION);
        }
    }

    private boolean isValidAccessToken(String accessToken) {
        return jwtTokenProvider.validateToken(accessToken) && !redisRepository.isTokenInBlacklist(accessToken);
    }

    private void handleJwtException(JwtException e, HttpServletResponse response) throws IOException, JSONException {
        switch (e) {
            case MalformedJwtException ignored -> setResponse(response, ErrorCode.INVALID_TOKEN_EXCEPTION);
            case ExpiredJwtException ignored -> setResponse(response, ErrorCode.JWT_TOKEN_EXPIRED);
            case UnsupportedJwtException ignored ->
                    setResponse(response, ErrorCode.UNSUPPORTED_JWT_TOKEN);
            case null, default -> setResponse(response, ErrorCode.JWT_COMPLEX_ERROR);
        }
    }

    // 에러 발생 시 Response 생성.
    private void setResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException, JSONException {
        JSONObject json = new JSONObject();
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        json.put("code", errorCode.getCode());
        json.put("message", errorCode.getMessage());

        response.getWriter().print(json);
        response.getWriter().flush();
    }

    private void setAuthentication(String token) {
        Authentication authentication = jwtTokenProvider.getAuthentication(token);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}

