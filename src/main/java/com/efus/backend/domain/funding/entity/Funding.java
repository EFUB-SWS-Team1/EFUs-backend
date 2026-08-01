package com.efus.backend.domain.funding.entity;

import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.global.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@Table(name = "funding")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Funding extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id", nullable = false)
    private OrganizationTerm organizationTerm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_term_member_id", nullable = false)
    private TermMember createdByTermMember;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long budgetAmount;

    @Column(nullable = false)
    private Integer participantCount;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Builder
    public Funding(OrganizationTerm organizationTerm, TermMember createdByTermMember, String name, Long budgetAmount, Integer participantCount, LocalDate startDate, LocalDate endDate) {
        this.organizationTerm = organizationTerm;
        this.createdByTermMember = createdByTermMember;
        this.name = name;
        this.budgetAmount = budgetAmount;
        this.participantCount = participantCount;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
