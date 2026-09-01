package com.ticketing.domain.auth;

import com.ticketing.domain.auth.dto.LoginRequest;
import com.ticketing.domain.auth.dto.SignupRequest;
import com.ticketing.domain.auth.dto.SignupResponse;
import com.ticketing.domain.auth.dto.TokenResponse;
import com.ticketing.domain.member.Member;
import com.ticketing.domain.member.MemberRepository;
import com.ticketing.global.error.BusinessException;
import com.ticketing.global.error.ErrorCode;
import com.ticketing.global.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public SignupResponse signup(SignupRequest request) {
        if (memberRepository.existsByEmail(request.email())) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        Member member =
                Member.create(
                        request.email(),
                        passwordEncoder.encode(request.password()),
                        request.nickname());
        try {
            memberRepository.save(member);
        } catch (DataIntegrityViolationException e) {
            // lost the race against a concurrent signup with the same email
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        log.info("member signed up: id={}", member.getId());
        return SignupResponse.from(member);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        Member member =
                memberRepository
                        .findByEmail(request.email())
                        .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_CREDENTIALS));
        if (!passwordEncoder.matches(request.password(), member.getPasswordHash())) {
            throw new BusinessException(ErrorCode.INVALID_CREDENTIALS);
        }
        return issueTokens(member);
    }

    @Transactional(readOnly = true)
    public TokenResponse refresh(String refreshToken) {
        Long memberId;
        try {
            memberId = tokenProvider.parseRefreshTokenSubject(refreshToken);
        } catch (RuntimeException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
        Member member =
                memberRepository
                        .findById(memberId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_TOKEN));
        return issueTokens(member);
    }

    private TokenResponse issueTokens(Member member) {
        String access = tokenProvider.issueAccessToken(member.getId(), member.getRole());
        String refresh = tokenProvider.issueRefreshToken(member.getId());
        return TokenResponse.bearer(access, refresh, tokenProvider.accessTokenTtlSeconds());
    }
}
