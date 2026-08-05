package com.efus.backend.domain.charge.dto.response;

import com.efus.backend.domain.charge.entity.ChargeMethod;
import com.efus.backend.domain.charge.entity.ChargePaymentStatus;
import java.time.LocalDate;

public record ChargeDetailResponse(
        Long chargeId,
        String title,
        ChargeMethod chargeMethod,
        LocalDate dueDate,
        Long fundingId,
        String fundingName,
        String memo,
        Long requestedAmount,
        Long paidAmount,
        Long unpaidAmount,
        long targetCount,
        long paidCount,
        long unpaidCount,
        ChargePaymentStatus paymentStatus
) {
}
