package com.ticketing.global.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI ticketingOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Ticketing Platform API")
                .description("대용량 트래픽 티켓 예매 플랫폼 백엔드 API")
                .version("v0.0.1"));
    }
}
