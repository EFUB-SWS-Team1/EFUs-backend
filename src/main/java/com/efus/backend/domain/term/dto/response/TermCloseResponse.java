package com.efus.backend.domain.term.dto.response;

import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record TermCloseResponse(
        Long termId, // 종료된 기수 ID[cite: 8]
        String name, // 기수명[cite: 8]
        LocalDate startDate, // 기수 시작일[cite: 8]
        LocalDate endDate, // 기수 종료일[cite: 8]
        String status, // CLOSED[cite: 8]
        ClosedByDto closedBy, // 기수를 종료한 운영진 정보[cite: 8]
        LocalDateTime closedAt // 실제 종료 처리 일시[cite: 8]
) {
    public record ClosedByDto(
            Long termMemberId, // 종료 처리자의 TermMember ID[cite: 8]
            String name // 종료 처리자의 이름[cite: 8]
    ) {}

    public static TermCloseResponse of(OrganizationTerm term, TermMember closer) {
        ClosedByDto closedByDto = new ClosedByDto(
                closer.getId(),
                closer.getUser().getName() // User 엔티티에서 이름을 가져온다고 가정
        );

        return new TermCloseResponse(
                term.getId(),
                term.getName(),
                term.getStartDate(),
                term.getEndDate(),
                term.getTermStatus().name(), // CLOSED
                closedByDto,
                LocalDateTime.now() // 실제 종료 처리 일시
        );
    }
}