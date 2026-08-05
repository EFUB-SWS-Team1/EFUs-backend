package com.efus.backend.domain.charge.dto.request;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChargeHistoryListRequest {

    @Min(0)
    private int page = 0;

    @Min(1)
    private int size = 20;
}
