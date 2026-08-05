package com.efus.backend.domain.member.dto.response;

import java.util.List;
import org.springframework.data.domain.Page;

public record TermMemberChargeListResponse(
        List<TermMemberChargeResponse> charges,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public TermMemberChargeListResponse {
        charges = charges == null ? List.of() : List.copyOf(charges);
    }

    public static TermMemberChargeListResponse from(Page<TermMemberChargeResponse> chargePage) {
        return new TermMemberChargeListResponse(
                chargePage.getContent(), chargePage.getNumber(), chargePage.getSize(),
                chargePage.getTotalElements(), chargePage.getTotalPages(), chargePage.hasNext()
        );
    }
}
