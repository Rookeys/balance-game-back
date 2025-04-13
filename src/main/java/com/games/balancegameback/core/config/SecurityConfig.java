package com.games.balancegameback.core.config;

import com.games.balancegameback.core.jwt.JwtAuthenticationTokenFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationTokenFilter jwtAuthenticationTokenFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                // CSRF 비활성화
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())

                // 권한 설정
                .authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers("/api/v1/admin/**").hasRole("ADMIN") // ADMIN 역할만 접근 가능
                            .requestMatchers(HttpMethod.GET, "/api/v1/users/exists").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/v1/users/test/login").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/v1/users/login/kakao").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/v1/users/login").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/v1/users/logout").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/v1/users/signup").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/v1/users/refresh").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/v1/users/cancel/resign").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/v1/media/single").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/v1/media/multiple").permitAll()
                            .requestMatchers(HttpMethod.POST, "/api/v1/games/{gameId}/play").permitAll()
                            .requestMatchers(HttpMethod.PUT, "/api/v1/games/{gameId}/play/{playId}").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/v1/games/{gameId}/play/{playId}").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/v1/games/{gameId}/play").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/v1/games/{gameId}/resources/{resourceId}").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/v1/games/{gameId}/resources/{resourceId}/comments").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/v1/games/{gameId}/resources/{resourceId}/comments/{parentId}").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/v1/games/{gameId}/results").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/v1/games/{gameId}/results/page").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/v1/games/{gameId}/results/comments").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/v1/games/list").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/v1/games/categories").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/v1/games/{gameId}").permitAll()
                            .requestMatchers(HttpMethod.GET, "/api/v1/games/{gameId}/resources/count").permitAll()
                            .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                            .anyRequest().authenticated(); // 그 외 모든 요청은 검증 필요
                })

                // JWT 필터 추가
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}



