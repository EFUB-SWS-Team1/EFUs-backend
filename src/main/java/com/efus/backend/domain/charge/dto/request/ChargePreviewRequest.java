package com.efus.backend.domain.charge.dto.request;

import com.efus.backend.domain.charge.entity.ChargeMethod;
import com.efus.backend.domain.charge.entity.ChargeTargetMode;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ChargePreviewRequest(
        @NotNull ChargeMethod chargeMethod,
        @NotNull ChargeTargetMode targetMode,
        List<Long> targetTermMemberIds,
        Long perPersonAmount,
        Long totalAmount
) {
}
