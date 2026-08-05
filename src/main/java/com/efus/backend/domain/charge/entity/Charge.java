package com.efus.backend.domain.charge.entity;

import com.efus.backend.domain.funding.entity.Funding;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.global.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "charge")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Charge extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_id", nullable = false)
    private OrganizationTerm term;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "funding_id")
    private Funding funding;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_term_member_id", nullable = false)
    private TermMember createdByTermMember;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChargeMethod chargeMethod;

    @Column(nullable = false)
    private LocalDate dueDate;

    private String memo;

    @Column(nullable = false)
    private Long requestedAmount;

    @Column(nullable = false)
    private boolean deleted;

    private LocalDateTime deletedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deleted_by_term_member_id")
    private TermMember deletedByTermMember;

    private Charge(OrganizationTerm term, Funding funding, TermMember createdByTermMember,
                   String title, ChargeMethod chargeMethod, LocalDate dueDate, String memo,
                   Long requestedAmount) {
        this.term = term;
        this.funding = funding;
        this.createdByTermMember = createdByTermMember;
        this.title = title;
        this.chargeMethod = chargeMethod;
        this.dueDate = dueDate;
        this.memo = memo;
        this.requestedAmount = requestedAmount;
        this.deleted = false;
    }

    public static Charge create(OrganizationTerm term, Funding funding, TermMember createdByTermMember,
                                String title, ChargeMethod chargeMethod, LocalDate dueDate, String memo,
                                Long requestedAmount) {
        return new Charge(term, funding, createdByTermMember, title, chargeMethod, dueDate, memo, requestedAmount);
    }
}
