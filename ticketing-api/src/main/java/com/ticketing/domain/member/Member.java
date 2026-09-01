package com.ticketing.domain.member;

import com.ticketing.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "member")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 320)
    private String email;

    /** BCrypt hash — never the raw password. */
    @Column(name = "password_hash", nullable = false, length = 100)
    private String passwordHash;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberRole role;

    private Member(String email, String passwordHash, String nickname, MemberRole role) {
        this.email = email;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.role = role;
    }

    public static Member create(String email, String passwordHash, String nickname) {
        return new Member(email, passwordHash, nickname, MemberRole.USER);
    }
}
