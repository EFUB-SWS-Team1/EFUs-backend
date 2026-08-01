package com.efus.backend.domain.organization.service;

import com.efus.backend.domain.invitation.repository.InvitationRepository;
import com.efus.backend.domain.invitation.service.InvitationCommandService;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.entity.TermMemberRole;
import com.efus.backend.domain.member.repository.MemberRepository;
import com.efus.backend.domain.member.repository.TermMemberRepository;
import com.efus.backend.domain.organization.dto.request.OrganizationCreateRequest;
import com.efus.backend.domain.organization.dto.response.OrganizationCreateResponse;
import com.efus.backend.domain.organization.dto.response.OrganizationDetailResponse;
import com.efus.backend.domain.organization.dto.response.OrganizationListResponse;
import com.efus.backend.domain.organization.dto.response.OrganizationResponse;
import com.efus.backend.domain.organization.entity.Organization;
import com.efus.backend.domain.organization.repository.OrganizationRepository;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.entity.TermStatus;
import com.efus.backend.domain.term.repository.TermRepository;
import com.efus.backend.domain.user.entity.User;
import com.efus.backend.domain.user.service.CurrentUserService;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import com.efus.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    private final CurrentUserService currentUserService;
    private final OrganizationRepository organizationRepository;
    private final TermRepository termRepository;
    private final MemberRepository memberRepository;
    private final TermMemberRepository termMemberRepository;
    private final InvitationCommandService invitationCommandService;

    // 단체 생성
    @Transactional
    public OrganizationCreateResponse createOrganization(OrganizationCreateRequest request) {
        // 유저 조회
        User user = currentUserService.getCurrentUser();

        // 1. 단체 생성
        Organization organization = request.toEntity(user);
        organizationRepository.save(organization);

        // 2. 기수 생성
        OrganizationTerm term = request.toTermEntity(organization, user);
        termRepository.save(term);

        // 3. 생성자를 첫 기수의 STAFF로 등록
        TermMember staffMember = TermMember.create(
                term,
                user,
                TermMemberRole.STAFF,
                LocalDateTime.now()
        );
        memberRepository.save(staffMember);

        // 4. 첫 기수의 STAFF/MEMBER용 초대 코드 생성
        invitationCommandService.createDefaultInvitations(term, staffMember);

        return OrganizationCreateResponse.of(organization, term, staffMember);
    }

//    @Transactional(readOnly = true)
//    public OrganizationListResponse getMyOrganizations() {
//        User user = currentUserService.getCurrentUser();
//
//        // 2. 유저가 가입한 단체 목록 조회
//        //List<Organization> myOrganizations
//
//        // 3. 세미나에서 배운 stream().map() 적용
//        List<OrganizationResponse> organizationResponses = myOrganizations.stream()
//                .map(organization -> {
//                    return OrganizationResponse.of(organization, organizationTerm, termMember, memberCount);
//                })
//                .toList();
//
//        // 4. ListResponse로 감싸서 반환
//        return new OrganizationListResponse(organizationResponses);
//    }

    @Transactional(readOnly = true)
    public OrganizationDetailResponse getOrganizationDetail(Long organizationId) {
        // 유저 조회
        User user = currentUserService.getCurrentUser();

        // 단체 조회
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORGANIZATION_NOT_FOUND));

        // 권한 검증
        validateOrganizationMember(organizationId, user.getId());

        // 현재 활성 기수 조회
        OrganizationTerm activeTerm = termRepository.findByOrganizationIdAndTermStatus(organizationId, TermStatus.ACTIVE)
                .orElse(null);

        Long memberCount = null;
        TermMember myActiveMember = null;

        // 활성 기수가 존재할 때만 멤버 수와 내 역할을 조회
        if (activeTerm != null) {
            memberCount = termMemberRepository.countByTermId(activeTerm.getId());
            myActiveMember = termMemberRepository.findByTermIdAndUserId(activeTerm.getId(), user.getId())
                    .orElse(null);
        }

        // 최근 종료 기수 조회
        OrganizationTerm recentClosedTerm = termRepository.findTopByOrganizationIdAndTermStatusOrderByEndDateDesc(organizationId, TermStatus.CLOSED)
                .orElse(null);

        return OrganizationDetailResponse.of(
                organization,
                activeTerm,
                recentClosedTerm,
                memberCount,
                myActiveMember
        );
    }

    private void validateOrganizationMember(Long organizationId, Long userId) {
        boolean isMember = termMemberRepository.existsByTerm_Organization_IdAndUser_Id(organizationId, userId);

        if (!isMember) {
            throw new CustomException(ErrorCode.ORGANIZATION_ACCESS_DENIED);
        }
    }
}
