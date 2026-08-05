package com.efus.backend.domain.charge.dto.response;

import com.efus.backend.domain.charge.entity.ChargeMember;
import com.efus.backend.domain.charge.entity.ChargeMemberPaymentStatus;
import com.efus.backend.domain.member.entity.TermMemberRole;
import java.time.LocalDateTime;

public record ChargeMemberResponse(
        Long chargeMemberId,
        Long termMemberId,
        String name,
        TermMemberRole role,
        Long assignedAmount,
        ChargeMemberPaymentStatus paymentStatus,
        LocalDateTime paidAt
) {
    public static ChargeMemberResponse from(ChargeMember chargeMember) {
        return new ChargeMemberResponse(
                chargeMember.getId(),
                chargeMember.getTermMember().getId(),
                chargeMember.getTermMember().getUser().getName(),
                chargeMember.getTermMember().getRole(),
                chargeMember.getAssignedAmount(),
                chargeMember.getPaymentStatus(),
                chargeMember.getPaidAt()
        );
    }
}
