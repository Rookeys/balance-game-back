package com.games.balancegameback.core.config;

import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public GroupedOpenApi defaultApi() {
        Info info = new Info().title("밸런스 게임 API").version("v0.1");

        return GroupedOpenApi.builder()
                .group("all")
                .pathsToMatch("/api/v1/**")
                .displayName("All API")
                .addOpenApiCustomiser(api -> api.setInfo(info))
                .build();
    }

    @Bean
    public GroupedOpenApi userApi() {
        Info info = new Info().title("유저 API").version("v0.1");

        return GroupedOpenApi.builder()
                .group("users")
                .pathsToMatch("/api/v1/users/**")
                .displayName("Users and Authorization")
                .addOpenApiCustomiser(api -> api.setInfo(info))
                .build();
    }

    @Bean
    public GroupedOpenApi gamesApi() {
        Info info = new Info().title("게임 API").version("v0.1");
        String[] paths = {"/api/v1/game"};

        return GroupedOpenApi.builder()
                .group("games")
                .pathsToMatch(paths)
                .displayName("Game's API")
                .addOpenApiCustomiser(api -> api.setInfo(info))
                .build();
    }

    @Bean
    public GroupedOpenApi supportApi() {
        Info info = new Info().title("S3 및 기타 API").version("v0.1");
        String[] paths = {"/api/v1/s3/**"};

        return GroupedOpenApi.builder()
                .group("supports")
                .pathsToMatch(paths)
                .displayName("Support's API")
                .addOpenApiCustomiser(api -> api.setInfo(info))
                .build();
    }
}
