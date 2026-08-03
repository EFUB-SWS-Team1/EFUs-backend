package com.efus.backend.domain.funding.dto.response;

import com.efus.backend.domain.funding.entity.Funding;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record FundingResponse(
        Long fundingId,
        Long termId,
        Long createdByTermMemberId,
        String name,
        Long budgetAmount,
        Integer participantCount,
        LocalDate startDate,
        LocalDate endDate,
        LocalDateTime createdAt
) {

    public static FundingResponse from(Funding funding) {
        return new FundingResponse(
                funding.getId(),
                funding.getOrganizationTerm().getId(),
                funding.getCreatedByTermMember().getId(),
                funding.getName(),
                funding.getBudgetAmount(),
                funding.getParticipantCount(),
                funding.getStartDate(),
                funding.getEndDate(),
                funding.getCreatedAt()
        );
    }
}

