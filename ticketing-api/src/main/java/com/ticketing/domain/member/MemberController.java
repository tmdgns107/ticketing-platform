package com.ticketing.domain.member;

import com.ticketing.global.common.ApiResponse;
import com.ticketing.global.error.BusinessException;
import com.ticketing.global.error.ErrorCode;
import com.ticketing.global.security.CurrentMember;
import com.ticketing.global.security.LoginMember;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members")
public class MemberController {

    private final MemberRepository memberRepository;

    @GetMapping("/me")
    @Transactional(readOnly = true)
    public ApiResponse<MemberResponse> me(@CurrentMember LoginMember loginMember) {
        Member member =
                memberRepository
                        .findById(loginMember.id())
                        .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));
        return ApiResponse.ok(MemberResponse.from(member));
    }

    public record MemberResponse(Long id, String email, String nickname, MemberRole role) {
        static MemberResponse from(Member member) {
            return new MemberResponse(
                    member.getId(), member.getEmail(), member.getNickname(), member.getRole());
        }
    }
}
