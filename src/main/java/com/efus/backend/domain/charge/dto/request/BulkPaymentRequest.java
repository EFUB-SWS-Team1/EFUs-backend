package com.efus.backend.domain.charge.dto.request;

import com.efus.backend.domain.charge.entity.BulkPaymentTargetMode;
import java.time.LocalDateTime;
import java.util.List;

public record BulkPaymentRequest(
        BulkPaymentTargetMode targetMode,
        List<Long> chargeMemberIds,
        LocalDateTime paidAt
) {
}
