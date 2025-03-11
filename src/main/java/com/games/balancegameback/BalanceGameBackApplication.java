package com.games.balancegameback;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import jakarta.persistence.EntityListeners;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
@EntityListeners(AuditingEntityListener.class)
@OpenAPIDefinition(
        servers = {
                @Server(url = "https://api.balancegame.site", description = "Default Server url"),
                @Server(url = "http://localhost:8888", description = "Local Server url")
        }
)
public class BalanceGameBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(BalanceGameBackApplication.class, args);
    }

}
