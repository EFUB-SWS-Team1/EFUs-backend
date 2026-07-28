package com.efus.backend.domain.term.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public record TermUpdateRequest(
        // 선택 필드이므로 @NotBlank, @NotNull 대신 Service에서 수동 검증(EMPTY_UPDATE_REQUEST) 처리
        String name,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        LocalDate startDate
) {
    // 요청 객체에 값이 하나도 없는지 확인하는 유틸 메서드
    public boolean isEmpty() {
        return (name == null || name.isBlank()) && startDate == null;
    }
}