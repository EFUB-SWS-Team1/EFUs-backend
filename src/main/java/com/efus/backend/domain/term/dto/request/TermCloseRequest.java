package com.efus.backend.domain.term.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record TermCloseRequest(
        @NotNull(message = "종료일은 필수입니다.") // 에러 코드: TERM_END_DATE_REQUIRED[cite: 8]
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul") // 에러 코드: INVALID_TERM_END_DATE[cite: 8]
        LocalDate endDate
) {
}