package com.efus.backend.domain.member.dto.response;

import com.efus.backend.domain.charge.dto.internal.MemberChargeSummary;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.entity.TermMemberRole;

public record TermMemberDetailResponse(
        String name,
        String email,
        TermMemberRole role,
        Long paidAmount,
        Long unpaidAmount
) {
    public static TermMemberDetailResponse of(
            TermMember termMember,
            MemberChargeSummary chargeSummary
    ) {
        return new TermMemberDetailResponse(
                termMember.getUser().getName(),
                termMember.getUser().getEmail(),
                termMember.getRole(),
                chargeSummary.paidAmount(),
                chargeSummary.unpaidAmount()
        );
    }
}
