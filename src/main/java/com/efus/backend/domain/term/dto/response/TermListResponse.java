package com.efus.backend.domain.term.dto.response;

import java.time.LocalDate;
import java.util.List;

public record TermListResponse(
        Long organizationId, // 단체 ID[cite: 5]
        String organizationName, // 단체명[cite: 5]
        List<TermDetailDto> terms // 단체에 속한 기수 목록[cite: 5]
) {
    public record TermDetailDto(
            Long termId,
            String name,
            LocalDate startDate,
            LocalDate endDate, // null 가능[cite: 5]
            String status, // ACTIVE 또는 CLOSED[cite: 5]
            Long memberCount, // 해당 기수 구성원 수[cite: 5]
            String myRole // 해당 기수에서 사용자의 역할 (가입하지 않았으면 null)
    ) {}
}