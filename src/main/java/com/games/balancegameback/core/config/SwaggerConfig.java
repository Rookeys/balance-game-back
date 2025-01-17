package com.games.balancegameback.core.config;

import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("밸런스 게임 API")
                        .version("v0.1")
                        .description("밸런스 게임 API 문서"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }

    @Bean
    public GroupedOpenApi defaultApi() {
        return GroupedOpenApi.builder()
                .group("all")
                .pathsToMatch("/api/v1/**")
                .displayName("All API")
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        return GroupedOpenApi.builder()
                .group("users")
                .pathsToMatch("/api/v1/users/**")
                .displayName("Users and Authorization")
                .build();
    }

    @Bean
    public GroupedOpenApi gameRoomApi() {
        return GroupedOpenApi.builder()
                .group("games")
                .pathsToMatch("/api/v1/game/room/**")
                .displayName("Game's API")
                .build();
    }

    @Bean
    public GroupedOpenApi gameResourceApi() {
        return GroupedOpenApi.builder()
                .group("resources")
                .pathsToMatch("/api/v1/game/resource/**")
                .displayName("Game Resource's API")
                .build();
    }

    @Bean
    public GroupedOpenApi mediaApi() {
        return GroupedOpenApi.builder()
                .group("Media")
                .pathsToMatch("/api/v1/media/**")
                .displayName("Media's API")
                .build();
    }
}
