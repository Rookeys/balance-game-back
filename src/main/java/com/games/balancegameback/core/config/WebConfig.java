package com.games.balancegameback.core.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Bean
    public RestTemplateBuilder restTemplateBuilder() {
        return new RestTemplateBuilder();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    @Override
    public void addCorsMappings(final CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000",
                        "https://front.balancegame.site",
                        "https://balance-game-front-deploy.vercel.app",
                        "http://localhost:8888",
                        "https://api.balancegame.site")
                .exposedHeaders("authorization", "refreshToken", "Set-Cookie")
                .allowedHeaders("*")
                .allowedMethods("*")
                .allowCredentials(true);
    }
}
