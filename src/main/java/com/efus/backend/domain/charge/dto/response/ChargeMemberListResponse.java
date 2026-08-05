package com.efus.backend.domain.charge.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

public record ChargeMemberListResponse(
        List<ChargeMemberResponse> members,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public ChargeMemberListResponse {
        members = members == null ? List.of() : List.copyOf(members);
    }

    public static ChargeMemberListResponse from(Page<ChargeMemberResponse> memberPage) {
        return new ChargeMemberListResponse(
                memberPage.getContent(), memberPage.getNumber(), memberPage.getSize(),
                memberPage.getTotalElements(), memberPage.getTotalPages(), memberPage.hasNext()
        );
    }
}
