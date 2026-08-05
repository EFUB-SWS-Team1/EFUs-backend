package com.efus.backend.domain.charge.dto.response;

import com.efus.backend.domain.member.entity.TermMember;

public record ChargePreviewMemberResponse(
        Long termMemberId,
        String name,
        Long assignedAmount
) {
    public static ChargePreviewMemberResponse of(TermMember termMember, Long assignedAmount) {
        return new ChargePreviewMemberResponse(
                termMember.getId(),
                termMember.getUser().getName(),
                assignedAmount
        );
    }
}
