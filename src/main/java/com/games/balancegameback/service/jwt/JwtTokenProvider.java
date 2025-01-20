package com.games.balancegameback.service.jwt;

import com.games.balancegameback.core.exception.ErrorCode;
import com.games.balancegameback.core.exception.impl.CustomJwtException;
import com.games.balancegameback.core.exception.impl.NotFoundException;
import com.games.balancegameback.domain.user.Users;
import com.games.balancegameback.domain.user.enums.UserRole;
import com.games.balancegameback.infra.repository.redis.RedisRepository;
import com.games.balancegameback.service.user.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

import org.springframework.security.core.Authentication;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final UserRepository userRepository;
    private final RedisRepository redisRepository;
    private final CustomUserDetailService customUserDetailService;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.accessTokenExpiration}")
    private long accessTokenValidTime;

    @Value("${jwt.refreshTokenExpiration}")
    private long refreshTokenValidTime;

    private Key signingKey;

    @PostConstruct
    protected void init() {
        this.signingKey = Keys.hmacShaKeyFor(Base64.getEncoder().encodeToString(secretKey.getBytes()).getBytes());
    }

    public String createAccessToken(String email, UserRole userRole) {
        return createToken(email, userRole, accessTokenValidTime);
    }

    public String createRefreshToken(String email, UserRole userRole) {
        return createToken(email, userRole, refreshTokenValidTime);
    }

    private String createToken(String email, UserRole userRole, long tokenValid) {
        Claims claims = Jwts.claims().setSubject(email);
        claims.put("roles", userRole.name());

        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + tokenValid))
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();
    }

    public String resolveAccessToken(HttpServletRequest request) {
        return extractToken(request.getHeader("Authorization"));
    }

    public String resolveRefreshToken(HttpServletRequest request) {
        return extractToken(request.getHeader("RefreshToken"));
    }

    // 공통 토큰 추출 로직
    private String extractToken(String header) {
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }

        return null;
    }

    // AccessToken 재발급
    public String reissueAccessToken(String refreshToken) {
        String email = validateRefreshTokenAndGetEmail(refreshToken);
        UserRole userRole = getRoles(email);
        return createAccessToken(email, userRole);
    }

    // RefreshToken 재발급
    public String reissueRefreshToken(String refreshToken) {
        String email = validateRefreshTokenAndGetEmail(refreshToken);
        String newRefreshToken = createRefreshToken(email, getRoles(email));

        redisRepository.delValues(refreshToken);
        redisRepository.setValues(newRefreshToken, email);

        return newRefreshToken;
    }

    private String validateRefreshTokenAndGetEmail(String refreshToken) {
        return redisRepository.getValues(refreshToken);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(signingKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (MalformedJwtException e) {
            throw new CustomJwtException(ErrorCode.INVALID_TOKEN_EXCEPTION, "4001");
        } catch (ExpiredJwtException e) {
            throw new CustomJwtException(ErrorCode.JWT_TOKEN_EXPIRED, "4002");
        } catch (UnsupportedJwtException e) {
            throw new CustomJwtException(ErrorCode.UNSUPPORTED_JWT_TOKEN, "4003");
        } catch (SignatureException e) {
            throw new CustomJwtException(ErrorCode.JWT_SIGNATURE_MISMATCH, "4005");
        }
    }

    public void setHeaderAccessToken(HttpServletResponse response, String accessToken) {
        response.setHeader("Authorization", "Bearer " + accessToken);
    }

    public void setHeaderRefreshToken(HttpServletResponse response, String refreshToken) {
        response.setHeader("RefreshToken", "Bearer " + refreshToken);
    }

    public UserRole getRoles(String email) {
        Optional<Users> users = userRepository.findByEmail(email);
        return users.isPresent() ? users.get().getUserRole() : UserRole.USER;
    }

    public Authentication getAuthentication(String token) {
        String email = extractEmail(token);
        UserDetails userDetails = customUserDetailService.loadUserByUsername(email);
        return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
    }

    public void expireToken(String token) {
        Key key = Keys.hmacShaKeyFor(secretKey.getBytes());
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        Date expiration = claims.getExpiration();
        Date now = new Date();
        if (now.after(expiration)) {
            redisRepository.addTokenToBlacklist(token, expiration.getTime() - now.getTime());
        }
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }
}

