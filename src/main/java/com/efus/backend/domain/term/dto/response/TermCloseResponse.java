package com.efus.backend.domain.term.dto.response;

import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TermCloseResponse(
        Long termId,
        String name,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        ClosedByDto closedBy,
        LocalDateTime closedAt
) {
    public record ClosedByDto(
            Long termMemberId,
            String name
    ) {}

    public static TermCloseResponse of(OrganizationTerm term, TermMember closer) {
        ClosedByDto closedByDto = new ClosedByDto(
                closer.getId(),
                closer.getUser().getName()
        );

        return new TermCloseResponse(
                term.getId(),
                term.getName(),
                term.getStartDate(),
                term.getEndDate(),
                term.getTermStatus().name(),
                closedByDto,
                LocalDateTime.now()
        );
    }
}