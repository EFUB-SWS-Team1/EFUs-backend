package com.efus.backend.domain.member.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

public record TermMemberListResponse(
        List<TermMemberResponse> members,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    public TermMemberListResponse {
        members = members == null ? List.of() : List.copyOf(members);
    }

    public static TermMemberListResponse from(Page<TermMemberResponse> memberPage) {
        return new TermMemberListResponse(
                memberPage.getContent(),
                memberPage.getNumber(),
                memberPage.getSize(),
                memberPage.getTotalElements(),
                memberPage.getTotalPages(),
                memberPage.hasNext()
        );
    }
}
