package com.efus.backend.domain.charge.service;

import com.efus.backend.domain.charge.dto.request.ChargeCreateRequest;
import com.efus.backend.domain.charge.dto.request.ChargePreviewRequest;
import com.efus.backend.domain.charge.dto.response.ChargeCreateResponse;
import com.efus.backend.domain.charge.dto.response.ChargePreviewMemberResponse;
import com.efus.backend.domain.charge.dto.response.ChargePreviewResponse;
import com.efus.backend.domain.charge.entity.Charge;
import com.efus.backend.domain.charge.entity.ChargeMember;
import com.efus.backend.domain.charge.entity.ChargeMethod;
import com.efus.backend.domain.charge.entity.ChargeTargetMode;
import com.efus.backend.domain.charge.repository.ChargeMemberRepository;
import com.efus.backend.domain.charge.repository.ChargeRepository;
import com.efus.backend.domain.funding.entity.Funding;
import com.efus.backend.domain.funding.service.FundingQueryService;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.service.MemberQueryService;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.service.TermQueryService;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ChargeService {

    private final TermQueryService termQueryService;
    private final MemberQueryService memberQueryService;
    private final FundingQueryService fundingQueryService;
    private final ChargeRepository chargeRepository;
    private final ChargeMemberRepository chargeMemberRepository;

    @Autowired
    public ChargeService(TermQueryService termQueryService, MemberQueryService memberQueryService,
                         FundingQueryService fundingQueryService, ChargeRepository chargeRepository,
                         ChargeMemberRepository chargeMemberRepository) {
        this.termQueryService = termQueryService;
        this.memberQueryService = memberQueryService;
        this.fundingQueryService = fundingQueryService;
        this.chargeRepository = chargeRepository;
        this.chargeMemberRepository = chargeMemberRepository;
    }

    // Preview unit tests and isolated callers do not need persistence collaborators.
    public ChargeService(TermQueryService termQueryService, MemberQueryService memberQueryService) {
        this(termQueryService, memberQueryService, null, null, null);
    }

    public ChargePreviewResponse preview(Long termId, ChargePreviewRequest request) {
        termQueryService.getTerm(termId);
        memberQueryService.validateActiveStaff(termId);
        termQueryService.validateActiveTerm(termId);

        Calculation calculation = calculate(termId, request.chargeMethod(), request.targetMode(),
                request.targetTermMemberIds(), request.perPersonAmount(), request.totalAmount());

        List<ChargePreviewMemberResponse> members = calculation.targets().stream()
                .map(target -> ChargePreviewMemberResponse.of(target, calculation.assignedAmount()))
                .toList();
        return new ChargePreviewResponse(calculation.totalAmount(), calculation.requestedAmount(),
                calculation.remainderAmount(), calculation.targets().size(), members);
    }

    @Transactional
    public ChargeCreateResponse createCharge(Long termId, ChargeCreateRequest request) {
        OrganizationTerm term = termQueryService.getTerm(termId);
        TermMember creator = memberQueryService.getCurrentActiveStaff(termId);
        termQueryService.validateActiveTerm(termId);

        Calculation calculation = calculate(termId, request.chargeMethod(), request.targetMode(),
                request.targetTermMemberIds(), request.perPersonAmount(), request.totalAmount());
        Funding funding = request.fundingId() == null
                ? null : fundingQueryService.getFundingInTerm(termId, request.fundingId());

        Charge charge = Charge.create(term, funding, creator, request.title(), request.chargeMethod(),
                request.dueDate(), request.memo(), calculation.requestedAmount());
        Charge savedCharge = chargeRepository.save(charge);
        List<ChargeMember> chargeMembers = calculation.targets().stream()
                .map(target -> ChargeMember.create(savedCharge, target, calculation.assignedAmount()))
                .toList();
        chargeMemberRepository.saveAll(chargeMembers);

        return new ChargeCreateResponse(savedCharge.getId(), savedCharge.getRequestedAmount(), chargeMembers.size());
    }

    private Calculation calculate(Long termId, ChargeMethod chargeMethod, ChargeTargetMode targetMode,
                                  List<Long> targetIds, Long perPersonAmount, Long totalAmount) {
        validateRequest(chargeMethod, targetMode, targetIds, perPersonAmount, totalAmount);
        List<TermMember> targets = resolveTargets(termId, targetMode, targetIds).stream()
                .sorted(Comparator.comparing(TermMember::getId))
                .toList();
        if (targets.isEmpty()) {
            throw new CustomException(ErrorCode.CHARGE_TARGET_NOT_FOUND);
        }

        if (chargeMethod == ChargeMethod.PER_PERSON) {
            try {
                long requestedAmount = Math.multiplyExact(perPersonAmount, (long) targets.size());
                return new Calculation(targets, perPersonAmount, requestedAmount, requestedAmount, 0L);
            } catch (ArithmeticException exception) {
                throw new CustomException(ErrorCode.INVALID_CHARGE_REQUEST);
            }
        }

        if (totalAmount < targets.size()) {
            throw new CustomException(ErrorCode.INVALID_CHARGE_REQUEST);
        }
        long assignedAmount = totalAmount / targets.size();
        long requestedAmount = assignedAmount * targets.size();
        return new Calculation(targets, assignedAmount, totalAmount, requestedAmount,
                totalAmount - requestedAmount);
    }

    private void validateRequest(ChargeMethod chargeMethod, ChargeTargetMode targetMode,
                                 List<Long> targetIds, Long perPersonAmount, Long totalAmount) {
        if (chargeMethod == null || targetMode == null) {
            throw new CustomException(ErrorCode.INVALID_CHARGE_REQUEST);
        }
        if (chargeMethod == ChargeMethod.PER_PERSON) {
            if (perPersonAmount == null || perPersonAmount <= 0 || totalAmount != null) {
                throw new CustomException(ErrorCode.INVALID_CHARGE_REQUEST);
            }
        } else if (totalAmount == null || totalAmount <= 0 || perPersonAmount != null) {
            throw new CustomException(ErrorCode.INVALID_CHARGE_REQUEST);
        }

        if (targetMode == ChargeTargetMode.ALL_ACTIVE) {
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

    private List<TermMember> resolveTargets(Long termId, ChargeTargetMode targetMode, List<Long> targetIds) {
        return targetMode == ChargeTargetMode.ALL_ACTIVE
                ? memberQueryService.getAllActiveMembers(termId)
                : memberQueryService.getSelectedActiveMembers(termId, targetIds);
    }

    private record Calculation(List<TermMember> targets, long assignedAmount, long totalAmount,
                               long requestedAmount, long remainderAmount) {
    }
}
