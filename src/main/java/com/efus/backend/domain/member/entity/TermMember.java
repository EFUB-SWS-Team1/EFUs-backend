package com.efus.backend.domain.member.entity;

import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.user.entity.User;
import com.efus.backend.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "term_member",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_term_member_term_user",
                        columnNames = {"term_id", "user_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TermMember extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id", nullable = false)
    private OrganizationTerm term;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TermMemberRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TermMemberStatus status;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Builder
    public TermMember(OrganizationTerm term, User user, TermMemberRole role, TermMemberStatus status, LocalDateTime joinedAt) {
        this.term = term;
        this.user = user;
        this.role = role;
        this.status = status;
        this.joinedAt = (joinedAt != null) ? joinedAt : LocalDateTime.now();
//    private TermMember(
//            OrganizationTerm term,
//            User user,
//            TermMemberRole role,
//            LocalDateTime joinedAt
//    ) {
//        this.term = term;
//        this.user = user;
//        this.role = role;
//        this.status = TermMemberStatus.ACTIVE;
//        this.joinedAt = joinedAt;
//    }
//
//    public static TermMember create(
//            OrganizationTerm term,
//            User user,
//            TermMemberRole role,
//            LocalDateTime joinedAt
//    ) {
//        return new TermMember(term, user, role, joinedAt);
    }

    public boolean isStaff() {
        return role == TermMemberRole.STAFF;
    }

    public boolean isActive() {
        return status == TermMemberStatus.ACTIVE;
    }

    public void deactivate() {
        this.status = TermMemberStatus.INACTIVE;
    }
}