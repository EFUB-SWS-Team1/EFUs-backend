package com.efus.backend.domain.charge.dto.request;

import com.efus.backend.domain.charge.entity.ChargeMethod;
import com.efus.backend.domain.charge.entity.ChargeTargetMode;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ChargeUpdateRequest {
    private String title;
    private LocalDate dueDate;
    private Long fundingId;
    private String memo;
    private ChargeMethod chargeMethod;
    private ChargeTargetMode targetMode;
    private List<Long> targetTermMemberIds;
    private Long perPersonAmount;
    private Long totalAmount;

    private boolean titlePresent;
    private boolean dueDatePresent;
    private boolean fundingIdPresent;
    private boolean memoPresent;
    private boolean chargeMethodPresent;
    private boolean targetModePresent;
    private boolean targetTermMemberIdsPresent;
    private boolean perPersonAmountPresent;
    private boolean totalAmountPresent;

    @JsonSetter public void setTitle(String value) { titlePresent = true; title = value; }
    @JsonSetter public void setDueDate(LocalDate value) { dueDatePresent = true; dueDate = value; }
    @JsonSetter public void setFundingId(Long value) { fundingIdPresent = true; fundingId = value; }
    @JsonSetter public void setMemo(String value) { memoPresent = true; memo = value; }
    @JsonSetter public void setChargeMethod(ChargeMethod value) { chargeMethodPresent = true; chargeMethod = value; }
    @JsonSetter public void setTargetMode(ChargeTargetMode value) { targetModePresent = true; targetMode = value; }
    @JsonSetter public void setTargetTermMemberIds(List<Long> value) { targetTermMemberIdsPresent = true; targetTermMemberIds = value; }
    @JsonSetter public void setPerPersonAmount(Long value) { perPersonAmountPresent = true; perPersonAmount = value; }
    @JsonSetter public void setTotalAmount(Long value) { totalAmountPresent = true; totalAmount = value; }

    public boolean hasBasicUpdate() { return titlePresent || dueDatePresent || fundingIdPresent || memoPresent; }
    public boolean hasCalculationUpdate() {
        return chargeMethodPresent || targetModePresent || targetTermMemberIdsPresent
                || perPersonAmountPresent || totalAmountPresent;
    }
    public boolean isEmpty() { return !hasBasicUpdate() && !hasCalculationUpdate(); }
}
