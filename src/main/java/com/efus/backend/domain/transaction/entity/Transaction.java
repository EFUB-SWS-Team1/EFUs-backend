package com.efus.backend.domain.transaction.entity;

// TODO: funding 연관관계 병합 후 주석 해제
//import com.efus.backend.domain.funding.entity.Funding;
// TODO: term 연관관계 병합 후 주석 해제
//import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.member.entity.TermMember;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "transactions")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // TODO: term 연관관계 병합 후 주석 해제
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "term_id", nullable = false)
//    private OrganizationTerm term;

    // TODO: funding 연관관계 병합 후 주석 해제
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "funding_id")
//    private Funding funding;

    // TODO: member 연관관계 병합 후 주석 해제
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "created_by_term_member_id", nullable = false)
//    private TermMember createdByTermMember;

    // TODO: member 연관관계 병합 후 주석 해제
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "updated_by_term_member_id")
//    private TermMember updatedByTermMember;

    // TODO: member 연관관계 병합 후 주석 해제
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "deleted_by_term_member_id")
//    private TermMember deletedByTermMember;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private TransactionType transactionType;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private Long amount;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(columnDefinition = "text")
    private String memo;

    @Column(nullable = false)
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Builder
    public Transaction(
            // TODO: term 연관관계 병합 후 주석 해제
//            OrganizationTerm term,
            // TODO: funding 연관관계 병합 후 주석 해제
//            Funding funding,
            TermMember createdByTermMember,
            TransactionType transactionType,
            String title,
            Long amount,
            LocalDate transactionDate,
            String memo
    ) {
        // TODO: term 연관관계 병합 후 주석 해제
//        this.term = term;
        // TODO: funding 연관관계 병합 후 주석 해제
//        this.funding = funding;
        // TODO: member 연관관계 병합 후 주석 해제
//        this.createdByTermMember = createdByTermMember;
        this.transactionType = transactionType;
        this.title = title;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.memo = memo;
        this.deleted = false;
    }

    public void update(
            // TODO: funding 연관관계 병합 후 주석 해제
//            Funding funding,
            TermMember updatedByTermMember,
            TransactionType transactionType,
            String title,
            Long amount,
            LocalDate transactionDate,
            String memo
    ) {
        // TODO: funding 연관관계 병합 후 주석 해제
//        this.funding = funding;
        // TODO: member 연관관계 병합 후 주석 해제
//        this.updatedByTermMember = updatedByTermMember;
        this.transactionType = transactionType;
        this.title = title;
        this.amount = amount;
        this.transactionDate = transactionDate;
        this.memo = memo;
    }

    public void softDelete(TermMember deletedByTermMember, LocalDateTime deletedAt) {
        this.deleted = true;
        // TODO: member 연관관계 병합 후 주석 해제
//        this.deletedByTermMember = deletedByTermMember;
        this.deletedAt = deletedAt;
    }

    public boolean isDeleted() {
        return deleted;
    }
}