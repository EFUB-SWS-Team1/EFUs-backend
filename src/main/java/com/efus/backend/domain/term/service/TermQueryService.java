package com.efus.backend.domain.term.service;

import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.repository.TermMemberRepository;
import com.efus.backend.domain.organization.entity.Organization;
import com.efus.backend.domain.organization.repository.OrganizationRepository;
import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.repository.TermMemberRepository;
import com.efus.backend.domain.organization.entity.Organization;
import com.efus.backend.domain.term.dto.response.TermDetailResponse;
import com.efus.backend.domain.term.dto.response.TermListResponse;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.entity.TermStatus;
import com.efus.backend.domain.term.repository.TermRepository;
import com.efus.backend.domain.user.entity.User;
import com.efus.backend.domain.user.service.CurrentUserService;
import com.efus.backend.global.exception.CustomException;
import com.efus.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermQueryService {

    private final TermRepository termRepository;
    private final CurrentUserService currentUserService;
    private final OrganizationRepository organizationRepository;
    private final TermMemberRepository termMemberRepository;

    // 기수 조회
    public OrganizationTerm getTerm(Long termId) {
        return termRepository.findById(termId)
                .orElseThrow(() -> new CustomException(ErrorCode.TERM_NOT_FOUND));
    }

    // 활성화된 기수인지 검증
    public void validateActiveTerm(Long termId) {
        OrganizationTerm term = getTerm(termId);

        if (term.getTermStatus() != TermStatus.ACTIVE) {
            throw new CustomException(ErrorCode.TERM_CLOSED);
        }
    }

    // [기수 상세 조회]
    @Transactional(readOnly = true)
    public TermDetailResponse getTermDetail(Long termId) {
        User user = currentUserService.getCurrentUser();
        OrganizationTerm term = getTerm(termId);
        Organization organization = term.getOrganization();

        // 사용자가 기수에 가입한 이력이 있는지 확인
        boolean isOrganizationMember = termMemberRepository.existsByTerm_Organization_IdAndUser_Id(
                organization.getId(), user.getId()
        );

        if (!isOrganizationMember) {
            throw new CustomException(ErrorCode.TERM_ACCESS_DENIED);
        }

        Long memberCount = termMemberRepository.countByTermId(term.getId());
        TermMember myTermMember = termMemberRepository.findByTermIdAndUserId(term.getId(), user.getId())
                .orElse(null);

        return TermDetailResponse.of(term, organization, memberCount, myTermMember);
    }

    // [기수 목록 조회]
    @Transactional(readOnly = true)
    public TermListResponse getTermList(Long organizationId) {
        User user = currentUserService.getCurrentUser();
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new CustomException(ErrorCode.ORGANIZATION_NOT_FOUND));

        boolean hasAccess = termMemberRepository.existsByTerm_Organization_IdAndUser_Id(organizationId, user.getId());
        if (!hasAccess) {
            throw new CustomException(ErrorCode.ORGANIZATION_ACCESS_DENIED);
        }

        List<OrganizationTerm> terms = termRepository.findByOrganizationIdOrderByStartDateDesc(organizationId);

        List<TermListResponse.TermDetailDto> termDtos = terms.stream()
                .map(term -> {
                    Long memberCount = termMemberRepository.countByTermId(term.getId());
                    TermMember myTermMember = termMemberRepository.findByTermIdAndUserId(term.getId(), user.getId())
                            .orElse(null);
                    return TermListResponse.TermDetailDto.of(term, memberCount, myTermMember);
                })
                .toList();

        return TermListResponse.of(organization, termDtos);
    }
}