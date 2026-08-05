package com.efus.backend.domain.member.dto.response;

import com.efus.backend.domain.charge.entity.ChargeMember;
import com.efus.backend.domain.charge.entity.ChargeMemberPaymentStatus;
import java.time.LocalDate;

public record TermMemberChargeResponse(
        Long chargeId,
        String title,
        Long assignedAmount,
        LocalDate dueDate,
        ChargeMemberPaymentStatus paymentStatus
) {
    public static TermMemberChargeResponse from(ChargeMember chargeMember) {
        return new TermMemberChargeResponse(
                chargeMember.getCharge().getId(),
                chargeMember.getCharge().getTitle(),
                chargeMember.getAssignedAmount(),
                chargeMember.getCharge().getDueDate(),
                chargeMember.getPaymentStatus()
        );
    }
}
