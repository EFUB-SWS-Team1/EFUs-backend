package com.efus.backend.domain.charge.dto.response;

import com.efus.backend.domain.charge.entity.ChargeMember;
import com.efus.backend.domain.charge.entity.ChargeMemberPaymentStatus;
import java.time.LocalDateTime;

public record ChargePaymentResponse(Long chargeId, Long chargeMemberId, Long termMemberId,
                                    Long assignedAmount, ChargeMemberPaymentStatus paymentStatus,
                                    LocalDateTime paidAt) {
    public static ChargePaymentResponse from(ChargeMember member) {
        return new ChargePaymentResponse(member.getCharge().getId(), member.getId(),
                member.getTermMember().getId(), member.getAssignedAmount(),
                member.getPaymentStatus(), member.getPaidAt());
    }
}
