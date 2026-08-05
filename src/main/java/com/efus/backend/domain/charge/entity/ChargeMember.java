package com.efus.backend.domain.charge.entity;

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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "charge_member", uniqueConstraints = @UniqueConstraint(
        name = "uk_charge_member_charge_term_member", columnNames = {"charge_id", "term_member_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChargeMember extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "charge_id", nullable = false)
    private Charge charge;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "term_member_id", nullable = false)
    private TermMember termMember;

    @Column(nullable = false)
    private Long assignedAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChargeMemberPaymentStatus paymentStatus;

    private LocalDateTime paidAt;

    private ChargeMember(Charge charge, TermMember termMember, Long assignedAmount) {
        this.charge = charge;
        this.termMember = termMember;
        this.assignedAmount = assignedAmount;
        this.paymentStatus = ChargeMemberPaymentStatus.UNPAID;
    }

    public static ChargeMember create(Charge charge, TermMember termMember, Long assignedAmount) {
        return new ChargeMember(charge, termMember, assignedAmount);
    }

    public void updateAssignedAmount(Long assignedAmount) {
        this.assignedAmount = assignedAmount;
    }
}
