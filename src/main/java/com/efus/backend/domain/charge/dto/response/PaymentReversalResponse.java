package com.efus.backend.domain.charge.dto.response;

import com.efus.backend.domain.charge.entity.ChargeMember;
import com.efus.backend.domain.charge.entity.ChargeMemberPaymentStatus;
import java.time.LocalDateTime;

public record PaymentReversalResponse(Long chargeId, Long chargeMemberId, Long termMemberId,
                                      Long assignedAmount, ChargeMemberPaymentStatus paymentStatus,
                                      LocalDateTime paidAt) {
    public static PaymentReversalResponse from(ChargeMember member) {
        return new PaymentReversalResponse(member.getCharge().getId(), member.getId(),
                member.getTermMember().getId(), member.getAssignedAmount(),
                member.getPaymentStatus(), member.getPaidAt());
    }
}
