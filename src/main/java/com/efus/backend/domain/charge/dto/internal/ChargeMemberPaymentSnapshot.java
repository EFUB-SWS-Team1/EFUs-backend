package com.efus.backend.domain.charge.dto.internal;

import com.efus.backend.domain.charge.entity.ChargeMember;
import com.efus.backend.domain.charge.entity.ChargeMemberPaymentStatus;
import java.time.LocalDateTime;

public record ChargeMemberPaymentSnapshot(Long chargeMemberId, Long termMemberId,
                                          Long assignedAmount,
                                          ChargeMemberPaymentStatus paymentStatus,
                                          LocalDateTime paidAt) {
    public static ChargeMemberPaymentSnapshot from(ChargeMember member) {
        return new ChargeMemberPaymentSnapshot(member.getId(), member.getTermMember().getId(),
                member.getAssignedAmount(), member.getPaymentStatus(), member.getPaidAt());
    }
}
