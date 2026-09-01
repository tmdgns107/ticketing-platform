package com.ticketing.global.security;

import com.ticketing.domain.member.MemberRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Issues and verifies HS256 JWTs. Access tokens carry the member id ({@code sub}),
 * role, and token type; refresh tokens carry only id + type.
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_TYPE = "typ";

    private final SecretKey key;
    private final JwtProperties properties;

    public JwtTokenProvider(JwtProperties properties) {
        this.properties = properties;
        byte[] keyBytes = properties.secret().getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public String issueAccessToken(Long memberId, MemberRole role) {
        return build(memberId, TokenType.ACCESS, role, properties.accessTokenTtl());
    }

    public String issueRefreshToken(Long memberId) {
        return build(memberId, TokenType.REFRESH, null, properties.refreshTokenTtl());
    }

    public long accessTokenTtlSeconds() {
        return properties.accessTokenTtl().toSeconds();
    }

    /**
     * Parses and validates the token, returning the principal. Only ACCESS tokens are
     * accepted for authentication.
     *
     * @throws JwtException if the token is malformed, expired, tampered, or not an access token
     */
    public LoginMember parseAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(properties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (!TokenType.ACCESS.name().equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new JwtException("not an access token");
        }
        Long memberId = Long.valueOf(claims.getSubject());
        MemberRole role = MemberRole.valueOf(claims.get(CLAIM_ROLE, String.class));
        return new LoginMember(memberId, role);
    }

    public Long parseRefreshTokenSubject(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .requireIssuer(properties.issuer())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        if (!TokenType.REFRESH.name().equals(claims.get(CLAIM_TYPE, String.class))) {
            throw new JwtException("not a refresh token");
        }
        return Long.valueOf(claims.getSubject());
    }

    private String build(Long memberId, TokenType type, MemberRole role, Duration ttl) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .issuer(properties.issuer())
                .subject(String.valueOf(memberId))
                .claim(CLAIM_TYPE, type.name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)))
                .signWith(key);
        if (role != null) {
            builder.claim(CLAIM_ROLE, role.name());
        }
        return builder.compact();
    }
}
