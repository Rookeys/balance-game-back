package com.games.balancegameback.core.config;

import com.games.balancegameback.core.jwt.JwtAuthenticationTokenFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

                // 권한 설정
                .authorizeHttpRequests(auth -> {
                    auth
//                            .requestMatchers("/api/v1/admin/**").hasRole("ADMIN") // ADMIN 역할만 접근 가능
//                            .requestMatchers("/api/v1/**").authenticated() // 인증된 사용자만 접근 가능
                            .anyRequest().permitAll(); // 그 외 모든 요청은 허용
                })

                // JWT 필터 추가
                .addFilterBefore(jwtAuthenticationTokenFilter, UsernamePasswordAuthenticationFilter.class)

                // 로그인 페이지 설정
                .formLogin(form -> form
                        .loginPage("/api/v1/users/login")
                        .loginPage("/api/v1/users/test/login")
                        .permitAll()
                )

                // 로그아웃 설정
                .logout(logout -> logout
                        .logoutUrl("/api/v1/users/logout")
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setStatus(HttpServletResponse.SC_OK);
                        })
                )

                .build();
    }
}



