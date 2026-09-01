package com.ticketing.global.config;

import com.ticketing.global.security.CurrentMemberArgumentResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(CorsProperties.class)
public class WebConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;
    private final CurrentMemberArgumentResolver currentMemberArgumentResolver =
            new CurrentMemberArgumentResolver();

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (corsProperties.allowedOrigins().isEmpty()) {
            return;
        }
        registry.addMapping("/api/**")
                .allowedOrigins(corsProperties.allowedOrigins().toArray(String[]::new))
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Location")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentMemberArgumentResolver);
    }
}
