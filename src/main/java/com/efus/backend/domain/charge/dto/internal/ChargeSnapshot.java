package com.efus.backend.domain.charge.dto.internal;

import com.efus.backend.domain.charge.entity.Charge;
import com.efus.backend.domain.charge.entity.ChargeMember;
import com.efus.backend.domain.charge.entity.ChargeMemberPaymentStatus;
import com.efus.backend.domain.charge.entity.ChargeMethod;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record ChargeSnapshot(String title, ChargeMethod chargeMethod, LocalDate dueDate,
                             Long fundingId, String memo, Long requestedAmount,
                             List<MemberSnapshot> members) {
    public static ChargeSnapshot from(Charge charge, List<ChargeMember> members) {
        return new ChargeSnapshot(charge.getTitle(), charge.getChargeMethod(), charge.getDueDate(),
                charge.getFunding() == null ? null : charge.getFunding().getId(), charge.getMemo(),
                charge.getRequestedAmount(), members.stream().map(MemberSnapshot::from).toList());
    }

    public record MemberSnapshot(Long termMemberId, Long assignedAmount,
                                 ChargeMemberPaymentStatus paymentStatus, LocalDateTime paidAt) {
        static MemberSnapshot from(ChargeMember member) {
            return new MemberSnapshot(member.getTermMember().getId(), member.getAssignedAmount(),
                    member.getPaymentStatus(), member.getPaidAt());
        }
    }
}
