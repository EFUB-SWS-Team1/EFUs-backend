package com.efus.backend.domain.member.dto.response;

import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.entity.TermMemberRole;

public record TermMemberResponse(
        Long termMemberId,
        String name,
        String profileImageUrl,
        TermMemberRole role
) {

    public static TermMemberResponse from(TermMember termMember) {
        return new TermMemberResponse(
                termMember.getId(),
                termMember.getUser().getName(),
                termMember.getUser().getProfileImageUrl(),
                termMember.getRole()
        );
    }
}
