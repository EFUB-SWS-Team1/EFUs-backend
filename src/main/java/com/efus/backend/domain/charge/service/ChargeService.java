package com.efus.backend.domain.charge.service;

import com.efus.backend.domain.charge.dto.request.ChargeCreateRequest;
import com.efus.backend.domain.charge.dto.request.ChargeMemberListRequest;
import com.efus.backend.domain.charge.dto.request.ChargePreviewRequest;
import com.efus.backend.domain.charge.dto.request.ChargeUpdateRequest;
import com.efus.backend.domain.charge.dto.internal.ChargeSnapshot;
import com.efus.backend.domain.charge.dto.response.ChargeCreateResponse;
import com.efus.backend.domain.charge.dto.response.ChargeDetailResponse;
import com.efus.backend.domain.charge.dto.response.ChargeMemberListResponse;
import com.efus.backend.domain.charge.dto.response.ChargeMemberResponse;
import com.efus.backend.domain.charge.dto.response.ChargePreviewMemberResponse;
import com.efus.backend.domain.charge.dto.response.ChargePreviewResponse;
import com.efus.backend.domain.charge.entity.Charge;
import com.efus.backend.domain.charge.entity.ChargeMember;
import com.efus.backend.domain.charge.entity.ChargeMethod;
import com.efus.backend.domain.charge.entity.ChargeMemberPaymentStatus;
import com.efus.backend.domain.charge.entity.ChargePaymentStatus;
import com.efus.backend.domain.charge.entity.ChargeTargetMode;
import com.efus.backend.domain.charge.entity.ChargeHistory;
import com.efus.backend.domain.charge.repository.ChargeHistoryRepository;
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
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.time.LocalDateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class ChargeService {

    private final TermQueryService termQueryService;
    private final MemberQueryService memberQueryService;
    private final FundingQueryService fundingQueryService;
    private final ChargeRepository chargeRepository;
    private final ChargeMemberRepository chargeMemberRepository;
    private final ChargeHistoryRepository chargeHistoryRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public ChargeService(TermQueryService termQueryService, MemberQueryService memberQueryService,
                         FundingQueryService fundingQueryService, ChargeRepository chargeRepository,
                         ChargeMemberRepository chargeMemberRepository,
                         ChargeHistoryRepository chargeHistoryRepository, ObjectMapper objectMapper) {
        this.termQueryService = termQueryService;
        this.memberQueryService = memberQueryService;
        this.fundingQueryService = fundingQueryService;
        this.chargeRepository = chargeRepository;
        this.chargeMemberRepository = chargeMemberRepository;
        this.chargeHistoryRepository = chargeHistoryRepository;
        this.objectMapper = objectMapper;
    }

    public ChargeService(TermQueryService termQueryService, MemberQueryService memberQueryService,
                         FundingQueryService fundingQueryService, ChargeRepository chargeRepository,
                         ChargeMemberRepository chargeMemberRepository) {
        this(termQueryService, memberQueryService, fundingQueryService, chargeRepository,
                chargeMemberRepository, null, null);
    }

    // Preview unit tests and isolated callers do not need persistence collaborators.
    public ChargeService(TermQueryService termQueryService, MemberQueryService memberQueryService) {
        this(termQueryService, memberQueryService, null, null, null, null, null);
    }

    public ChargeDetailResponse getChargeDetail(Long chargeId) {
        Charge charge = getCharge(chargeId);
        memberQueryService.validateTermMember(charge.getTerm().getId());

        List<ChargeMember> chargeMembers = chargeMemberRepository.findAllByChargeId(chargeId);
        long paidAmount = chargeMembers.stream()
                .filter(member -> member.getPaymentStatus() == ChargeMemberPaymentStatus.PAID)
                .mapToLong(ChargeMember::getAssignedAmount)
                .sum();
        long targetCount = chargeMembers.size();
        long paidCount = chargeMembers.stream()
                .filter(member -> member.getPaymentStatus() == ChargeMemberPaymentStatus.PAID)
                .count();
        long unpaidCount = chargeMembers.stream()
                .filter(member -> member.getPaymentStatus() == ChargeMemberPaymentStatus.UNPAID)
                .count();
        long requestedAmount = charge.getRequestedAmount();
        ChargePaymentStatus paymentStatus = calculatePaymentStatus(
                requestedAmount, paidAmount, targetCount, paidCount);
        Funding funding = charge.getFunding();

        return new ChargeDetailResponse(
                charge.getId(), charge.getTitle(), charge.getChargeMethod(), charge.getDueDate(),
                funding == null ? null : funding.getId(), funding == null ? null : funding.getName(),
                charge.getMemo(), requestedAmount, paidAmount, requestedAmount - paidAmount,
                targetCount, paidCount, unpaidCount, paymentStatus);
    }

    public ChargeMemberListResponse getChargeMembers(Long chargeId, ChargeMemberListRequest request) {
        Charge charge = getCharge(chargeId);
        memberQueryService.validateTermMember(charge.getTerm().getId());

        String keyword = StringUtils.hasText(request.getKeyword()) ? request.getKeyword().trim() : null;
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<ChargeMemberResponse> members = chargeMemberRepository.findChargeMembers(
                chargeId, keyword, request.getPaymentStatus(), pageable
        ).map(ChargeMemberResponse::from);

        return ChargeMemberListResponse.from(members);
    }

    private Charge getCharge(Long chargeId) {
        return chargeRepository.findById(chargeId)
                .orElseThrow(() -> new CustomException(ErrorCode.CHARGE_NOT_FOUND));
    }

    private ChargePaymentStatus calculatePaymentStatus(
            long requestedAmount, long paidAmount, long targetCount, long paidCount) {
        if (paidAmount == 0L || paidCount == 0L) {
            return ChargePaymentStatus.UNPAID;
        }
        if (paidAmount == requestedAmount || paidCount == targetCount) {
            return ChargePaymentStatus.PAID;
        }
        return ChargePaymentStatus.PARTIALLY_PAID;
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

    @Transactional
    public ChargeDetailResponse updateCharge(Long chargeId, ChargeUpdateRequest request) {
        Charge charge = getCharge(chargeId);
        Long termId = charge.getTerm().getId();
        TermMember actor = memberQueryService.getCurrentActiveStaff(termId);
        termQueryService.validateActiveTerm(termId);
        if (charge.isDeleted()) {
            throw new CustomException(ErrorCode.CHARGE_ALREADY_DELETED);
        }
        if (request == null || request.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_CHARGE_REQUEST);
        }

        boolean hasPaidMember = chargeMemberRepository.existsByChargeIdAndPaymentStatus(
                chargeId, ChargeMemberPaymentStatus.PAID);
        if (hasPaidMember && request.hasCalculationUpdate()) {
            throw new CustomException(ErrorCode.CHARGE_UPDATE_RESTRICTED_AFTER_PAYMENT);
        }

        List<ChargeMember> members = chargeMemberRepository.findAllByChargeId(chargeId);
        ChargeSnapshot before = ChargeSnapshot.from(charge, members);
        validateBasicUpdate(request);

        String title = request.isTitlePresent() ? request.getTitle() : charge.getTitle();
        java.time.LocalDate dueDate = request.isDueDatePresent() ? request.getDueDate() : charge.getDueDate();
        String memo = request.isMemoPresent() ? request.getMemo() : charge.getMemo();
        Funding funding = request.isFundingIdPresent()
                ? (request.getFundingId() == null ? null
                : fundingQueryService.getFundingInTerm(termId, request.getFundingId()))
                : charge.getFunding();
        ChargeMethod method = charge.getChargeMethod();
        long requestedAmount = charge.getRequestedAmount();

        if (request.hasCalculationUpdate()) {
            method = request.isChargeMethodPresent() ? request.getChargeMethod() : charge.getChargeMethod();
            if (method == null) {
                throw new CustomException(ErrorCode.INVALID_CHARGE_REQUEST);
            }
            boolean targetUpdate = request.isTargetModePresent() || request.isTargetTermMemberIdsPresent();
            List<TermMember> targets;
            if (targetUpdate) {
                if (!request.isTargetModePresent() || request.getTargetMode() == null) {
                    throw new CustomException(ErrorCode.INVALID_CHARGE_REQUEST);
                }
                targets = resolveTargetsForUpdate(termId, request);
            } else {
                targets = members.stream().map(ChargeMember::getTermMember).toList();
            }

            Long perPersonAmount = request.isPerPersonAmountPresent() ? request.getPerPersonAmount() : null;
            Long totalAmount = request.isTotalAmountPresent() ? request.getTotalAmount() : null;
            if (!request.isPerPersonAmountPresent() && !request.isTotalAmountPresent()
                    && !request.isChargeMethodPresent() && !targetUpdate) {
                throw new CustomException(ErrorCode.INVALID_CHARGE_REQUEST);
            }
            if (!request.isPerPersonAmountPresent() && !request.isTotalAmountPresent()) {
                if (method == ChargeMethod.PER_PERSON && !members.isEmpty() && !targetUpdate) {
                    perPersonAmount = members.get(0).getAssignedAmount();
                } else {
                    throw new CustomException(ErrorCode.INVALID_CHARGE_REQUEST);
                }
            }
            Calculation calculation = calculateWithTargets(method, targets, perPersonAmount, totalAmount);
            requestedAmount = calculation.requestedAmount();

            if (targetUpdate) {
                chargeMemberRepository.deleteAllByChargeId(chargeId);
                chargeMemberRepository.flush();
                members = calculation.targets().stream()
                        .map(target -> ChargeMember.create(charge, target, calculation.assignedAmount()))
                        .toList();
                chargeMemberRepository.saveAll(members);
            } else {
                members.forEach(member -> member.updateAssignedAmount(calculation.assignedAmount()));
            }
        }

        charge.update(title, dueDate, funding, memo, method, requestedAmount);
        ChargeSnapshot after = ChargeSnapshot.from(charge, members);
        chargeHistoryRepository.save(ChargeHistory.updated(charge, actor, toJson(before), toJson(after)));
        return getChargeDetail(chargeId);
    }

    @Transactional
    public void deleteCharge(Long chargeId) {
        Charge charge = getCharge(chargeId);
        if (charge.isDeleted()) {
            throw new CustomException(ErrorCode.CHARGE_ALREADY_DELETED);
        }

        Long termId = charge.getTerm().getId();
        TermMember actor = memberQueryService.getCurrentActiveStaff(termId);
        termQueryService.validateActiveTerm(termId);
        if (chargeMemberRepository.existsByChargeIdAndPaymentStatus(
                chargeId, ChargeMemberPaymentStatus.PAID)) {
            throw new CustomException(ErrorCode.CHARGE_HAS_PAYMENT);
        }

        List<ChargeMember> members = chargeMemberRepository.findAllByChargeId(chargeId);
        ChargeSnapshot before = ChargeSnapshot.from(charge, members);
        charge.softDelete(actor, LocalDateTime.now());
        ChargeSnapshot after = ChargeSnapshot.from(charge, members);
        chargeHistoryRepository.save(ChargeHistory.deleted(
                charge, actor, toJson(before), toJson(after)));
    }

    private void validateBasicUpdate(ChargeUpdateRequest request) {
        if (request.isTitlePresent() && !StringUtils.hasText(request.getTitle())) {
            throw new CustomException(ErrorCode.INVALID_CHARGE_REQUEST);
        }
        if (request.isDueDatePresent() && request.getDueDate() == null) {
            throw new CustomException(ErrorCode.INVALID_CHARGE_REQUEST);
        }
    }

    private List<TermMember> resolveTargetsForUpdate(Long termId, ChargeUpdateRequest request) {
        validateTargetRequest(request.getTargetMode(), request.isTargetTermMemberIdsPresent()
                ? request.getTargetTermMemberIds() : null);
        return resolveTargets(termId, request.getTargetMode(), request.getTargetTermMemberIds());
    }

    private void validateTargetRequest(ChargeTargetMode mode, List<Long> ids) {
        if (mode == ChargeTargetMode.ALL_ACTIVE) {
            if (ids != null) throw new CustomException(ErrorCode.INVALID_CHARGE_REQUEST);
            return;
        }
        if (mode != ChargeTargetMode.SELECTED || ids == null || ids.isEmpty()) {
            throw new CustomException(ErrorCode.CHARGE_TARGET_REQUIRED);
        }
        if (ids.stream().anyMatch(id -> id == null || id <= 0)) {
            throw new CustomException(ErrorCode.INVALID_CHARGE_TARGET);
        }
        if (new HashSet<>(ids).size() != ids.size()) {
            throw new CustomException(ErrorCode.DUPLICATE_CHARGE_TARGET);
        }
    }

    private String toJson(ChargeSnapshot snapshot) {
        try {
            return objectMapper.writeValueAsString(snapshot);
        } catch (JsonProcessingException exception) {
            throw new CustomException(ErrorCode.CHARGE_HISTORY_SERIALIZATION_FAILED);
        }
    }

    private Calculation calculate(Long termId, ChargeMethod chargeMethod, ChargeTargetMode targetMode,
                                  List<Long> targetIds, Long perPersonAmount, Long totalAmount) {
        validateRequest(chargeMethod, targetMode, targetIds, perPersonAmount, totalAmount);
        List<TermMember> targets = resolveTargets(termId, targetMode, targetIds).stream()
                .sorted(Comparator.comparing(TermMember::getId))
                .toList();
        return calculateWithTargets(chargeMethod, targets, perPersonAmount, totalAmount);
    }

    private Calculation calculateWithTargets(ChargeMethod chargeMethod, List<TermMember> targets,
                                              Long perPersonAmount, Long totalAmount) {
        if (targets.isEmpty()) {
            throw new CustomException(ErrorCode.CHARGE_TARGET_NOT_FOUND);
        }

        if (chargeMethod == ChargeMethod.PER_PERSON) {
            if (perPersonAmount == null || perPersonAmount <= 0 || totalAmount != null) {
                throw new CustomException(ErrorCode.INVALID_CHARGE_REQUEST);
            }
        } else if (chargeMethod != ChargeMethod.EQUAL_SPLIT || totalAmount == null
                || totalAmount <= 0 || perPersonAmount != null) {
            throw new CustomException(ErrorCode.INVALID_CHARGE_REQUEST);
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
