package com.efus.backend.domain.term.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;

public record TermUpdateRequest(
        String name,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
        LocalDate startDate
) {
    public boolean isEmpty() {
        return (name == null || name.isBlank()) && startDate == null;
    }
}