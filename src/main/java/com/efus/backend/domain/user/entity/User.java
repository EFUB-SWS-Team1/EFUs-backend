package com.efus.backend.domain.user.entity;

import com.efus.backend.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long kakaoId;

    @Column
    private String email;

    @Column(nullable = false)
    private String name;

    @Column
    private String profileImageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Builder
    public User(Long kakaoId, String email, String name, String profileImageUrl, Status status) {
        this.kakaoId = kakaoId;
        this.email = email;
        this.name = name;
        this.profileImageUrl = profileImageUrl;
        this.status = status;
    }
}