package com.efus.backend.domain.term.dto.response;

import com.efus.backend.domain.organization.entity.Organization;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TermCreateResponse(
        Long termId,
        Long organizationId,
        String organizationName,
        String name,
        LocalDate startDate,
        LocalDate endDate, // 생성 직후 null[cite: 4]
        String status, // ACTIVE[cite: 4]
        Long myTermMemberId,
        String myRole, // STAFF[cite: 4]
        LocalDateTime createdAt
) {
    // 실제 구현 시 TermMember 엔티티를 받아 ID와 Role을 주입합니다.
    public static TermCreateResponse of(OrganizationTerm term, Organization org, Long myTermMemberId, String myRole) {
        return new TermCreateResponse(
                term.getId(),
                org.getId(),
                org.getName(),
                term.getName(),
                term.getStartDate(),
                term.getEndDate(),
                term.getTermStatus().name(),
                myTermMemberId,
                myRole,
                term.getCreatedAt()
        );
    }
}