package com.ticketing.global.security;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param secret            Base64-encoded HMAC key, at least 256 bits. Supplied via env in real envs.
 * @param accessTokenTtl    lifetime of the short-lived access token
 * @param refreshTokenTtl   lifetime of the refresh token
 * @param issuer            {@code iss} claim
 */
@ConfigurationProperties(prefix = "app.jwt")
public record JwtProperties(
        String secret,
        Duration accessTokenTtl,
        Duration refreshTokenTtl,
        String issuer
) {
    public JwtProperties {
        if (accessTokenTtl == null) {
            accessTokenTtl = Duration.ofMinutes(30);
        }
        if (refreshTokenTtl == null) {
            refreshTokenTtl = Duration.ofDays(14);
        }
        if (issuer == null || issuer.isBlank()) {
            issuer = "ticketing-platform";
        }
    }
}
