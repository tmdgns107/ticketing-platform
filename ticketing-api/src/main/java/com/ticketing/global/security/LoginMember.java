package com.ticketing.global.security;

import com.ticketing.domain.member.MemberRole;

/**
 * Authenticated principal carried in the SecurityContext. Built purely from JWT claims —
 * no DB lookup on the hot path.
 */
public record LoginMember(Long id, MemberRole role) {
}
