package com.ticketing.global.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ticketing.domain.waitingqueue.QueueAdmissionInterceptor;
import com.ticketing.domain.waitingqueue.WaitingQueueProperties;
import com.ticketing.domain.waitingqueue.WaitingQueueService;
import com.ticketing.global.security.CurrentMemberArgumentResolver;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(CorsProperties.class)
public class WebConfig implements WebMvcConfigurer {

    private final CorsProperties corsProperties;
    private final WaitingQueueService waitingQueueService;
    private final WaitingQueueProperties waitingQueueProperties;
    private final ObjectMapper objectMapper;
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

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        var queueAdmissionInterceptor =
                new QueueAdmissionInterceptor(
                        waitingQueueService, waitingQueueProperties, objectMapper);
        registry.addInterceptor(queueAdmissionInterceptor).addPathPatterns("/api/v1/reservations");
    }
}
