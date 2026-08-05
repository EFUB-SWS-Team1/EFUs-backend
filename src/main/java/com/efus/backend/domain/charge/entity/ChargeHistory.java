package com.efus.backend.domain.charge.entity;

import com.efus.backend.domain.member.entity.TermMember;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "charge_histories")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChargeHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "charge_id", nullable = false)
    private Charge charge;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "charge_member_id")
    private ChargeMember chargeMember;
    @ManyToOne(fetch = FetchType.LAZY, optional = false) @JoinColumn(name = "actor_term_member_id", nullable = false)
    private TermMember actorTermMember;
    @Enumerated(EnumType.STRING) @Column(name = "action_type", nullable = false, length = 30)
    private ChargeHistoryActionType actionType;
    @Column(nullable = false) private String summary;
    @Column(name = "before_data", columnDefinition = "json") private String beforeData;
    @Column(name = "after_data", columnDefinition = "json") private String afterData;
    @Column(name = "changed_at", nullable = false) private LocalDateTime changedAt;

    private ChargeHistory(Charge charge, TermMember actor, ChargeHistoryActionType actionType,
                          String summary, String beforeData, String afterData) {
        this.charge = charge;
        this.actorTermMember = actor;
        this.actionType = actionType;
        this.summary = summary;
        this.beforeData = beforeData;
        this.afterData = afterData;
        this.changedAt = LocalDateTime.now();
    }

    public static ChargeHistory updated(Charge charge, TermMember actor, String beforeData, String afterData) {
        return new ChargeHistory(charge, actor, ChargeHistoryActionType.CHARGE_UPDATED,
                "회비 청구 정보를 수정했습니다.", beforeData, afterData);
    }

    public static ChargeHistory paymentCompleted(Charge charge, ChargeMember chargeMember,
                                                 TermMember actor, String beforeData, String afterData) {
        ChargeHistory history = new ChargeHistory(charge, actor,
                ChargeHistoryActionType.PAYMENT_COMPLETED,
                "회비 개별 납부를 완료 처리했습니다.", beforeData, afterData);
        history.chargeMember = chargeMember;
        return history;
    }

    public static ChargeHistory deleted(Charge charge, TermMember actor, String beforeData, String afterData) {
        return new ChargeHistory(charge, actor, ChargeHistoryActionType.DELETE,
                "회비 청구를 삭제했습니다.", beforeData, afterData);
    }
}
