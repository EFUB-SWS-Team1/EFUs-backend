package com.efus.backend.domain.term.dto.response;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TermDetailResponse(
        Long termId, // 기수 ID[cite: 6]
        OrganizationDto organization, // 기수가 속한 단체 정보[cite: 6]
        String name, // 기수명[cite: 6]
        LocalDate startDate, // 기수 시작일[cite: 6]
        LocalDate endDate, // 기수 종료일 (null 가능)
        String status, // ACTIVE 또는 CLOSED
        Long memberCount, // 기수 구성원 수[cite: 6]
        Long myTermMemberId, // 로그인 사용자의 TermMember ID (null 가능)[cite: 6]
        String myRole, // 로그인 사용자의 역할 (null 가능)[cite: 6]
        LocalDateTime createdAt, // 기수 생성 일시[cite: 6]
        LocalDateTime updatedAt // 기수 정보 마지막 수정 일시[cite: 6]
) {
    public record OrganizationDto(
            Long organizationId, // 단체 ID[cite: 6]
            String name // 단체명[cite: 6]
    ) {}
}