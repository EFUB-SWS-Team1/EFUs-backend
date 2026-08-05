package com.efus.backend.domain.member.service;

import com.efus.backend.domain.charge.dto.internal.MemberChargeSummary;
import com.efus.backend.domain.charge.service.ChargeSummaryService;
import com.efus.backend.domain.member.dto.request.TermMemberListRequest;
import com.efus.backend.domain.member.dto.response.TermMemberListResponse;
import com.efus.backend.domain.member.dto.response.TermMemberDetailResponse;
import com.efus.backend.domain.member.dto.response.TermMemberResponse;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.entity.TermMemberStatus;
import com.efus.backend.domain.member.repository.TermMemberRepository;
import com.efus.backend.domain.term.service.TermQueryService;
import com.efus.backend.domain.user.entity.User;
import com.efus.backend.domain.user.service.CurrentUserService;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@Transactional(readOnly = true)
public class MemberQueryService {

    private final CurrentUserService currentUserService;
    private final TermQueryService termQueryService;
    private final TermMemberRepository termMemberRepository;
    private final ChargeSummaryService chargeSummaryService;

    @Autowired
    public MemberQueryService(
            CurrentUserService currentUserService,
            TermQueryService termQueryService,
            TermMemberRepository termMemberRepository,
            ChargeSummaryService chargeSummaryService
    ) {
        this.currentUserService = currentUserService;
        this.termQueryService = termQueryService;
        this.termMemberRepository = termMemberRepository;
        this.chargeSummaryService = chargeSummaryService;
    }

    public MemberQueryService(
            CurrentUserService currentUserService,
            TermQueryService termQueryService,
            TermMemberRepository termMemberRepository
    ) {
        this(currentUserService, termQueryService, termMemberRepository, null);
    }

    public TermMember getCurrentTermMember(Long termId) {
        User currentUser = currentUserService.getCurrentUser();

        return termMemberRepository.findByTermIdAndUserId(termId, currentUser.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.TERM_ACCESS_DENIED));
    }

    public void validateTermMember(Long termId) {
        getCurrentTermMember(termId);
    }

    public void validateStaff(Long termId) {
        TermMember currentTermMember = getCurrentTermMember(termId);

        if (!currentTermMember.isStaff()) {
            throw new CustomException(ErrorCode.STAFF_REQUIRED);
        }
    }

    public void validateActiveStaff(Long termId) {
        getCurrentActiveStaff(termId);
    }

    public TermMember getCurrentActiveStaff(Long termId) {
        TermMember currentTermMember = getCurrentTermMember(termId);

        if (!currentTermMember.isStaff()) {
            throw new CustomException(ErrorCode.STAFF_REQUIRED);
        }

        if (!currentTermMember.isActive()) {
            throw new CustomException(ErrorCode.INACTIVE_TERM_MEMBER);
        }

        return currentTermMember;
    }

    public List<TermMember> getAllActiveMembers(Long termId) {
        return termMemberRepository.findAllByTermIdAndStatus(termId, TermMemberStatus.ACTIVE);
    }

    public List<TermMember> getSelectedActiveMembers(Long termId, List<Long> termMemberIds) {
        List<TermMember> members = termMemberRepository.findAllByTermIdAndIdIn(termId, termMemberIds);

        if (members.size() != termMemberIds.size()) {
            throw new CustomException(ErrorCode.INVALID_CHARGE_TARGET);
        }

        if (members.stream().anyMatch(member -> !member.isActive())) {
            throw new CustomException(ErrorCode.INACTIVE_CHARGE_TARGET);
        }

        return members;
    }

    public TermMemberListResponse getTermMembers(
            Long termId,
            TermMemberListRequest request
    ) {
        termQueryService.getTerm(termId);
        validateTermMember(termId);

        String keyword = normalizeKeyword(request.getKeyword());
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());

        Page<TermMemberResponse> memberPage = termMemberRepository
                .findTermMembers(termId, keyword, request.getRole(), pageable)
                .map(TermMemberResponse::from);

        return TermMemberListResponse.from(memberPage);
    }

    public TermMemberDetailResponse getTermMemberDetail(Long termId, Long termMemberId) {
        termQueryService.getTerm(termId);
        validateTermMember(termId);

        TermMember termMember = termMemberRepository.findByIdAndTermId(termMemberId, termId)
                .orElseThrow(() -> new CustomException(ErrorCode.TERM_MEMBER_NOT_FOUND));
        MemberChargeSummary summary = chargeSummaryService
                .calculateMemberChargeSummary(termId, termMemberId);

        return TermMemberDetailResponse.of(termMember, summary);
    }

    private String normalizeKeyword(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }

        return keyword.trim();
    }
}
