package com.efus.backend.domain.charge.service;

import com.efus.backend.domain.charge.dto.request.ChargePreviewRequest;
import com.efus.backend.domain.charge.dto.response.ChargePreviewMemberResponse;
import com.efus.backend.domain.charge.dto.response.ChargePreviewResponse;
import com.efus.backend.domain.charge.entity.ChargeMethod;
import com.efus.backend.domain.charge.entity.ChargeTargetMode;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.service.MemberQueryService;
import com.efus.backend.domain.term.service.TermQueryService;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChargeService {

    private final TermQueryService termQueryService;
    private final MemberQueryService memberQueryService;

    public ChargePreviewResponse preview(Long termId, ChargePreviewRequest request) {
        termQueryService.getTerm(termId);
        memberQueryService.validateActiveStaff(termId);
        termQueryService.validateActiveTerm(termId);

        validateRequest(request);
        List<TermMember> targets = resolveTargets(termId, request);

        if (targets.isEmpty()) {
            throw new CustomException(ErrorCode.CHARGE_TARGET_NOT_FOUND);
        }

        List<TermMember> sortedTargets = targets.stream()
                .sorted(Comparator.comparing(TermMember::getId))
                .toList();

        if (request.chargeMethod() == ChargeMethod.PER_PERSON) {
            return calculatePerPerson(sortedTargets, request.perPersonAmount());
        }

        return calculateEqualSplit(sortedTargets, request.totalAmount());
    }

    private void validateRequest(ChargePreviewRequest request) {
        if (request == null || request.chargeMethod() == null || request.targetMode() == null) {
            throw new CustomException(ErrorCode.INVALID_CHARGE_REQUEST);
        }

        if (request.chargeMethod() == ChargeMethod.PER_PERSON) {
            if (request.perPersonAmount() == null
                    || request.perPersonAmount() <= 0
                    || request.totalAmount() != null) {
                throw new CustomException(ErrorCode.INVALID_CHARGE_REQUEST);
            }
        } else if (request.totalAmount() == null
                || request.totalAmount() <= 0
                || request.perPersonAmount() != null) {
            throw new CustomException(ErrorCode.INVALID_CHARGE_REQUEST);
        }

        validateTargetRequest(request);
    }

    private void validateTargetRequest(ChargePreviewRequest request) {
        List<Long> targetIds = request.targetTermMemberIds();

        if (request.targetMode() == ChargeTargetMode.ALL_ACTIVE) {
            if (targetIds != null) {
                throw new CustomException(ErrorCode.INVALID_CHARGE_REQUEST);
            }
            return;
        }

        if (targetIds == null || targetIds.isEmpty()) {
            throw new CustomException(ErrorCode.CHARGE_TARGET_REQUIRED);
        }

        if (targetIds.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new CustomException(ErrorCode.INVALID_CHARGE_TARGET);
        }

        if (new HashSet<>(targetIds).size() != targetIds.size()) {
            throw new CustomException(ErrorCode.DUPLICATE_CHARGE_TARGET);
        }
    }

    private List<TermMember> resolveTargets(Long termId, ChargePreviewRequest request) {
        if (request.targetMode() == ChargeTargetMode.ALL_ACTIVE) {
            return memberQueryService.getAllActiveMembers(termId);
        }

        return memberQueryService.getSelectedActiveMembers(termId, request.targetTermMemberIds());
    }

    private ChargePreviewResponse calculatePerPerson(List<TermMember> targets, Long perPersonAmount) {
        long requestedAmount;
        try {
            requestedAmount = Math.multiplyExact(perPersonAmount, (long) targets.size());
        } catch (ArithmeticException exception) {
            throw new CustomException(ErrorCode.INVALID_CHARGE_REQUEST);
        }

        List<ChargePreviewMemberResponse> members = targets.stream()
                .map(target -> ChargePreviewMemberResponse.of(target, perPersonAmount))
                .toList();

        return new ChargePreviewResponse(
                requestedAmount,
                requestedAmount,
                0L,
                targets.size(),
                members
        );
    }

    private ChargePreviewResponse calculateEqualSplit(List<TermMember> targets, Long totalAmount) {
        if (totalAmount < targets.size()) {
            throw new CustomException(ErrorCode.INVALID_CHARGE_REQUEST);
        }

        long assignedAmount = totalAmount / targets.size();
        long requestedAmount = assignedAmount * targets.size();
        long remainderAmount = totalAmount - requestedAmount;

        List<ChargePreviewMemberResponse> members = targets.stream()
                .map(target -> ChargePreviewMemberResponse.of(target, assignedAmount))
                .toList();

        return new ChargePreviewResponse(
                totalAmount,
                requestedAmount,
                remainderAmount,
                targets.size(),
                members
        );
    }
}
