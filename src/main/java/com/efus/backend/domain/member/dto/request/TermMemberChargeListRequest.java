package com.efus.backend.domain.member.dto.request;

import com.efus.backend.domain.charge.entity.ChargeMemberPaymentStatus;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TermMemberChargeListRequest {

    private ChargeMemberPaymentStatus paymentStatus;

    @Min(0)
    private int page = 0;

    @Min(1)
    private int size = 7;
}
