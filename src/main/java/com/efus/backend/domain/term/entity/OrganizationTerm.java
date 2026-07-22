package com.efus.backend.domain.term.entity;

import com.efus.backend.domain.organization.entity.Organization;
import com.efus.backend.domain.user.entity.User;
import com.efus.backend.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "organization_term")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrganizationTerm extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TermStatus termStatus;

    @Builder
    public OrganizationTerm(Organization organization, User createdByUser, String name, LocalDate startDate, LocalDate endDate, TermStatus termStatus) {
        this.organization = organization;
        this.createdByUser = createdByUser;
        this.name = name;
        this.startDate = startDate;
        this.endDate = endDate;
        this.termStatus = termStatus;
    }
}
