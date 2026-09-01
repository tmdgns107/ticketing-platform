package com.ticketing.domain.auth.dto;

import com.ticketing.domain.member.Member;

public record SignupResponse(Long id, String email, String nickname) {
    public static SignupResponse from(Member member) {
        return new SignupResponse(member.getId(), member.getEmail(), member.getNickname());
    }
}
