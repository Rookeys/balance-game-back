/*
 * File Name   : BalanceGameBackApplication.java
 * Description : Spring Boot 애플리케이션 진입점
 *
 * Created By  : cheomuk
 * Created At  : 2026-05-04
 * Updated At  : 2026-05-04
 *
 * Change Log
 * -------------------------------------------------
 * 2026-05-04  @EnableCaching 추가
 */
package com.games.balancegameback;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.persistence.EntityListeners;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@EnableCaching
@SpringBootApplication
@EntityListeners(AuditingEntityListener.class)
@OpenAPIDefinition(
        servers = {
                @Server(url = "https://api.zznpk.com", description = "Default Server url"),
                @Server(url = "http://localhost:8888", description = "Local Server url")
        }
)
public class BalanceGameBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(BalanceGameBackApplication.class, args);
    }
}
