package com.ticketing.global.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.ticketing.domain.member.MemberRole;
import io.jsonwebtoken.JwtException;
import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtTokenProviderTest {

  private JwtTokenProvider provider;

  @BeforeEach
  void setUp() {
    var props =
        new JwtProperties(
            "unit-test-secret-unit-test-secret-0123456789",
            Duration.ofMinutes(30),
            Duration.ofDays(14),
            "ticketing-platform");
    provider = new JwtTokenProvider(props);
  }

  @Test
  void accessTokenRoundTripsToPrincipal() {
    String token = provider.issueAccessToken(42L, MemberRole.USER);

    LoginMember principal = provider.parseAccessToken(token);

    assertThat(principal.id()).isEqualTo(42L);
    assertThat(principal.role()).isEqualTo(MemberRole.USER);
  }

  @Test
  void refreshTokenIsNotAcceptedAsAccessToken() {
    String refresh = provider.issueRefreshToken(42L);

    assertThatThrownBy(() -> provider.parseAccessToken(refresh)).isInstanceOf(JwtException.class);
  }

  @Test
  void tamperedTokenIsRejected() {
    String token = provider.issueAccessToken(42L, MemberRole.USER);
    String tampered = token.substring(0, token.length() - 2) + "xx";

    assertThatThrownBy(() -> provider.parseAccessToken(tampered)).isInstanceOf(JwtException.class);
  }

  @Test
  void tokenSignedWithAnotherKeyIsRejected() {
    var otherProvider =
        new JwtTokenProvider(
            new JwtProperties(
                "different-secret-different-secret-987654321",
                Duration.ofMinutes(30),
                Duration.ofDays(14),
                "ticketing-platform"));
    String foreign = otherProvider.issueAccessToken(1L, MemberRole.USER);

    assertThatThrownBy(() -> provider.parseAccessToken(foreign)).isInstanceOf(JwtException.class);
  }
}
