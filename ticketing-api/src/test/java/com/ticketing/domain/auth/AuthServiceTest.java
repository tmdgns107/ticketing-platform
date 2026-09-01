package com.ticketing.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ticketing.domain.auth.dto.LoginRequest;
import com.ticketing.domain.auth.dto.SignupRequest;
import com.ticketing.domain.member.Member;
import com.ticketing.domain.member.MemberRepository;
import com.ticketing.domain.member.MemberRole;
import com.ticketing.global.error.BusinessException;
import com.ticketing.global.error.ErrorCode;
import com.ticketing.global.security.JwtTokenProvider;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  @Mock MemberRepository memberRepository;
  @Mock PasswordEncoder passwordEncoder;
  @Mock JwtTokenProvider tokenProvider;

  @InjectMocks AuthService authService;

  @Test
  void signupRejectsDuplicateEmail() {
    given(memberRepository.existsByEmail("a@b.com")).willReturn(true);

    assertThatThrownBy(() -> authService.signup(new SignupRequest("a@b.com", "password1", "nick")))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.EMAIL_ALREADY_EXISTS);

    verify(memberRepository, never()).save(any());
  }

  @Test
  void loginWithWrongPasswordFails() {
    Member member = Member.create("a@b.com", "hashed", "nick");
    given(memberRepository.findByEmail("a@b.com")).willReturn(Optional.of(member));
    given(passwordEncoder.matches("wrong", "hashed")).willReturn(false);

    assertThatThrownBy(() -> authService.login(new LoginRequest("a@b.com", "wrong")))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
  }

  @Test
  void loginWithUnknownEmailFails() {
    given(memberRepository.findByEmail("nobody@b.com")).willReturn(Optional.empty());

    assertThatThrownBy(() -> authService.login(new LoginRequest("nobody@b.com", "whatever")))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
  }

  @Test
  void loginIssuesBearerTokensOnSuccess() {
    Member member = Member.create("a@b.com", "hashed", "nick");
    given(memberRepository.findByEmail("a@b.com")).willReturn(Optional.of(member));
    given(passwordEncoder.matches("password1", "hashed")).willReturn(true);
    given(tokenProvider.issueAccessToken(any(), any())).willReturn("access-token");
    given(tokenProvider.issueRefreshToken(any())).willReturn("refresh-token");
    given(tokenProvider.accessTokenTtlSeconds()).willReturn(1800L);

    var response = authService.login(new LoginRequest("a@b.com", "password1"));

    assertThat(response.accessToken()).isEqualTo("access-token");
    assertThat(response.refreshToken()).isEqualTo("refresh-token");
    assertThat(response.tokenType()).isEqualTo("Bearer");
    assertThat(response.expiresIn()).isEqualTo(1800L);
  }

  @Test
  void memberRoleAuthorityIsPrefixed() {
    assertThat(MemberRole.USER.authority()).isEqualTo("ROLE_USER");
  }
}
