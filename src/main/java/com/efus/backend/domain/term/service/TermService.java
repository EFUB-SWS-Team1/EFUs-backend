package com.efus.backend.domain.term.service;

import com.efus.backend.domain.invitation.service.InvitationCommandService;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.entity.TermMemberRole;
import com.efus.backend.domain.member.repository.TermMemberRepository;
import com.efus.backend.domain.organization.entity.Organization;
import com.efus.backend.domain.organization.repository.OrganizationRepository;
import com.efus.backend.domain.term.dto.request.TermCloseRequest;
import com.efus.backend.domain.term.dto.request.TermCreateRequest;
import com.efus.backend.domain.term.dto.request.TermUpdateRequest;
import com.efus.backend.domain.term.dto.response.*;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.entity.TermStatus;
import com.efus.backend.domain.term.repository.TermRepository;
import com.efus.backend.domain.user.entity.User;
import com.efus.backend.domain.user.service.CurrentUserService;
// import com.efus.backend.domain.organization.service.OrganizationQueryService;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class TermService {
    private final CurrentUserService currentUserService;
    private final TermRepository termRepository;
    private final TermMemberRepository termMemberRepository;
    private final OrganizationRepository organizationRepository;
    private final InvitationCommandService invitationCommandService;
    private final TermQueryService termQueryService;

    // [다음 기수 생성]
    @Transactional
    public TermCreateResponse createTerm(Long organizationId, TermCreateRequest request) {
        User user = currentUserService.getCurrentUser();

        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORGANIZATION_NOT_FOUND));

        validateStaffInRecentTerm(organizationId, user.getId());

        boolean hasActiveTerm = termRepository.existsByOrganizationIdAndTermStatus(organizationId, TermStatus.ACTIVE);
        if (hasActiveTerm) {
            throw new CustomException(ErrorCode.ACTIVE_TERM_ALREADY_EXISTS);
        }

        // 위 검증들을 다 통과하면 새로운 기수 생성
        OrganizationTerm term = request.toEntity(organization, user);
        termRepository.save(term);
        TermMember staffMember = TermMember.create(
                term,
                user,
                TermMemberRole.STAFF,
                LocalDateTime.now()
        );
        termMemberRepository.save(staffMember);
        invitationCommandService.createDefaultInvitations(term, staffMember);

         return TermCreateResponse.of(term, organization, staffMember);
    }

    // [기수 정보 수정]
    @Transactional
    public TermUpdateResponse updateTerm(Long termId, TermUpdateRequest request) {
        if (request.isEmpty()) {
            throw new CustomException(ErrorCode.EMPTY_UPDATE_REQUEST);
        }

        User user = currentUserService.getCurrentUser();
        OrganizationTerm term = termQueryService.getTerm(termId);

        // 검증
        if (term.getTermStatus() == TermStatus.CLOSED) {
            throw new CustomException(ErrorCode.TERM_ALREADY_CLOSED);
        }
        validateCurrentTermStaff(termId, user.getId());

        // 정보 수정
        term.update(request.name(), request.startDate());

        return TermUpdateResponse.from(term);
    }

    // [기수 종료]
    @Transactional
    public TermCloseResponse closeTerm(Long termId, TermCloseRequest request) {
        User user = currentUserService.getCurrentUser();
        OrganizationTerm term = termQueryService.getTerm(termId);

        if (term.getTermStatus() == TermStatus.CLOSED) {
            throw new CustomException(ErrorCode.TERM_ALREADY_CLOSED);
        }

        TermMember closer = getAndValidateCurrentTermStaff(termId, user.getId());

        if (request.endDate().isBefore(term.getStartDate())) {
            throw new CustomException(ErrorCode.END_DATE_BEFORE_START_DATE);
        }

        term.close(request.endDate());
        invitationCommandService.deactivateCodesByTermId(termId);

        return TermCloseResponse.of(term, closer);
    }

    // [대시보드 조회]
    @Transactional(readOnly = true)
    public TermDashboardResponse getTermDashboard(Long termId) {

    }

    private void validateStaffInRecentTerm(Long organizationId, Long userId) {
        // 단체의 가장 최근 종료된 기수 조회
        OrganizationTerm recentTerm = termRepository.findTopByOrganizationIdAndTermStatusOrderByEndDateDesc(organizationId, TermStatus.CLOSED)
                .orElseThrow(() -> new CustomException(ErrorCode.ACTIVE_TERM_ALREADY_EXISTS));

        // 가장 최근 기수에 해당 유저가 소속되어 있었는지 확인
        TermMember recentTermMember = termMemberRepository.findByTermIdAndUserId(recentTerm.getId(), userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TERM_CREATION_FORBIDDEN));

        // 소속되어 있었다면, 그 역할이 STAFF인지 확인
        if (recentTermMember.getRole() != TermMemberRole.STAFF) {
            throw new CustomException(ErrorCode.TERM_CREATION_FORBIDDEN);
        }
    }

    private void validateCurrentTermStaff(Long termId, Long userId) {
        TermMember currentTermMember = termMemberRepository.findByTermIdAndUserId(termId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TERM_MEMBER_NOT_FOUND));

        if (currentTermMember.getRole() != TermMemberRole.STAFF) {
            throw new CustomException(ErrorCode.STAFF_PERMISSION_REQUIRED);
        }
    }

    private TermMember getAndValidateCurrentTermStaff(Long termId, Long userId) {
        TermMember currentTermMember = termMemberRepository.findByTermIdAndUserId(termId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.TERM_MEMBER_NOT_FOUND));

        if (currentTermMember.getRole() != TermMemberRole.STAFF) {
            throw new CustomException(ErrorCode.STAFF_PERMISSION_REQUIRED);
        }

        return currentTermMember;
    }


}