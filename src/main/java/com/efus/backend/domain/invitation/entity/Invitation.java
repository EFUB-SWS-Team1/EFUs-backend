package com.efus.backend.domain.invitation.entity;

import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.entity.TermMemberRole;

// TODO: term 도메인 병합 후 주석 해제
// import com.efus.backend.domain.term.entity.OrganizationTerm;

import com.efus.backend.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(
        name = "invitation",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_invitation_code",
                        columnNames = "code"
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invitation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * TODO: term 도메인 병합 후 주석 해제
     *
     * @ManyToOne(fetch = FetchType.LAZY, optional = false)
     * @JoinColumn(name = "term_id", nullable = false)
     * private OrganizationTerm term;
     */

//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "created_by_term_member_id", nullable = false)
//    private TermMember createdByTermMember;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TermMemberRole role;

    @Column(nullable = false)
    private boolean active;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    private Invitation(
            // TODO: term 도메인 병합 후 주석 해제
            // OrganizationTerm term,
            TermMember createdByTermMember,
            String code,
            TermMemberRole role,
            LocalDateTime expiresAt
    ) {
        // TODO: term 도메인 병합 후 주석 해제
        // this.term = term;

        //this.createdByTermMember = createdByTermMember;
        this.code = code;
        this.role = role;
        this.active = true;
        this.expiresAt = expiresAt;
    }

    public static Invitation create(
            // TODO: term 도메인 병합 후 주석 해제
            // OrganizationTerm term,
            TermMember createdByTermMember,
            String code,
            TermMemberRole role,
            LocalDateTime expiresAt
    ) {
        return new Invitation(
                // TODO: term 도메인 병합 후 주석 해제
                // term,
                createdByTermMember,
                code,
                role,
                expiresAt
        );
    }

    public void deactivate() {
        this.active = false;
    }

    public boolean isExpired(LocalDateTime currentTime) {
        return !expiresAt.isAfter(currentTime);
    }

    public boolean isAvailable(LocalDateTime currentTime) {
        return active && !isExpired(currentTime);
    }
}