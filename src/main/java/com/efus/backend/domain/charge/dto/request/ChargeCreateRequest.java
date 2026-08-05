package com.efus.backend.domain.charge.dto.request;

import com.efus.backend.domain.charge.entity.ChargeMethod;
import com.efus.backend.domain.charge.entity.ChargeTargetMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

public record ChargeCreateRequest(
        @NotBlank String title,
        @NotNull ChargeMethod chargeMethod,
        @NotNull LocalDate dueDate,
        Long fundingId,
        String memo,
        @NotNull ChargeTargetMode targetMode,
        List<Long> targetTermMemberIds,
        Long perPersonAmount,
        Long totalAmount
) {
}
