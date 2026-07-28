package com.efus.backend.domain.term.dto.response;

import com.efus.backend.domain.term.entity.OrganizationTerm;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TermUpdateResponse(
        Long termId, // 수정된 기수 ID[cite: 7]
        Long organizationId, // 단체 ID[cite: 7]
        String name, // 수정된 기수명[cite: 7]
        LocalDate startDate, // 수정된 시작일[cite: 7]
        LocalDate endDate, // 기수 종료일[cite: 7]
        String status, // 기수 상태[cite: 7]
        LocalDateTime updatedAt // 수정 일시[cite: 7]
) {
    public static TermUpdateResponse from(OrganizationTerm term) {
        return new TermUpdateResponse(
                term.getId(),
                term.getOrganization().getId(), // 외래키로 연결된 Organization에서 ID 추출
                term.getName(),
                term.getStartDate(),
                term.getEndDate(),
                term.getTermStatus().name(),
                term.getUpdatedAt() // BaseEntity에서 제공한다고 가정
        );
    }
}