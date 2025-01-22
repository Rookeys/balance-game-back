package com.games.balancegameback.core.jwt;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.CustomJwtException;
import com.games.balancegameback.infra.repository.redis.RedisRepository;
import com.games.balancegameback.service.jwt.JwtTokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.log4j.Log4j2;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Set;

@RequiredArgsConstructor
@Component
@Log4j2
public class JwtAuthenticationTokenFilter extends OncePerRequestFilter {

    private static final Set<String> EXCLUDED_PATHS = Set.of(
            "/swagger-ui/**", "/v3/api-docs/**", "/api/v1/users/login", "/api/v1/users/test/login",
            "/api/v1/users/signup", "/api/v1/media/single", "/api/v1/media/multiple", "/api/v1/users/exists"
    );

    private static final Set<String> REQUIRED_PATHS = Set.of(
            "/api/v1/users/refresh", "/api/v1/users/logout"
    );

    private final JwtTokenProvider jwtTokenProvider;
    private final RedisRepository redisRepository;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

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
            if (accessToken == null && refreshToken == null) {
                throw new CustomJwtException(ErrorCode.EMPTY_JWT_CLAIMS, "4004");
            }

            if (accessToken != null && refreshToken != null) {
                throw new CustomJwtException(ErrorCode.JWT_NOT_ALLOW_REQUEST, "잘못된 요청입니다.");
            }

            if (refreshToken == null) {
                if (isValidAccessToken(accessToken)) {
                    setAuthentication(accessToken);
                }
            }

            if (accessToken == null) {
                handleRefreshToken(refreshToken, path, filterChain, request, response);
                return;
            }
        } catch (CustomJwtException e) {
            setResponse(response, e.getErrorCode());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean skipPath(String path) {
        return EXCLUDED_PATHS.stream().anyMatch(excludedPath -> pathMatcher.match(excludedPath, path));
    }

    private boolean requiredPath(String path) {
        return REQUIRED_PATHS.stream().anyMatch(requiredPath -> pathMatcher.match(requiredPath, path));
    }

    private boolean isValidAccessToken(String accessToken) {
        return jwtTokenProvider.validateToken(accessToken);
    }

    private void handleRefreshToken(String refreshToken, String path, FilterChain filterChain,
                                    HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException, JSONException {
        if (jwtTokenProvider.validateToken(refreshToken) && redisRepository.isRefreshTokenValid(refreshToken)
                && requiredPath(path)) {
            filterChain.doFilter(request, response);
        } else {
            throw new CustomJwtException(ErrorCode.JWT_NOT_ALLOW_REQUEST, "4007");
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

