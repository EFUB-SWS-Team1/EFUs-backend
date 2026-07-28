package com.efus.backend.domain.transaction.entity;

import com.efus.backend.domain.member.entity.TermMember;
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
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "transaction_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TransactionHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    private Transaction transaction;

    // TODO: member 연관관계 병합 후 주석 해제
//    @ManyToOne(fetch = FetchType.LAZY, optional = false)
//    @JoinColumn(name = "actor_term_member_id", nullable = false)
//    private TermMember actorTermMember;

    @Enumerated(EnumType.STRING)
    @Column(name = "action_type", nullable = false, length = 20)
    private TransactionActionType actionType;

    @Column(nullable = false)
    private String summary;

    @Column(name = "before_data", columnDefinition = "json")
    private String beforeData;

    @Column(name = "after_data", columnDefinition = "json")
    private String afterData;

    @Column(name = "changed_at", nullable = false)
    private LocalDateTime changedAt;

    @Builder
    public TransactionHistory(
            Transaction transaction,
            TermMember actorTermMember,
            TransactionActionType actionType,
            String summary,
            String beforeData,
            String afterData,
            LocalDateTime changedAt
    ) {
        this.transaction = transaction;
        // TODO: member 연관관계 병합 후 주석 해제
//        this.actorTermMember = actorTermMember;
        this.actionType = actionType;
        this.summary = summary;
        this.beforeData = beforeData;
        this.afterData = afterData;
        this.changedAt = changedAt;
    }
}

