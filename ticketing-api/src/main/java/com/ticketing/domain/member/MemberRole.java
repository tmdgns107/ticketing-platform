package com.ticketing.domain.member;

public enum MemberRole {
    USER,
    ADMIN;

    public String authority() {
        return "ROLE_" + name();
    }
}
