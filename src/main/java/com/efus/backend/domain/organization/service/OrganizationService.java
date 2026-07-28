package com.efus.backend.domain.organization.service;

import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.repository.MemberRepository;
import com.efus.backend.domain.organization.dto.request.OrganizationCreateRequest;
import com.efus.backend.domain.organization.dto.response.OrganizationCreateResponse;
import com.efus.backend.domain.organization.dto.response.OrganizationListResponse;
import com.efus.backend.domain.organization.entity.Organization;
import com.efus.backend.domain.organization.repository.OrganizationRepository;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.entity.TermStatus;
import com.efus.backend.domain.term.repository.TermRepository;
import com.efus.backend.domain.user.entity.User;
import com.efus.backend.domain.user.service.CurrentUserService;
import com.efus.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrganizationService {
    private final CurrentUserService currentUserService;
    private final OrganizationRepository organizationRepository;
    private final TermRepository termRepository;
    private final MemberRepository memberRepository;

    // 단체 생성
//    @Transactional
//    public OrganizationCreateResponse createOrganization(OrganizationCreateRequest request) {
//        // 유저 조회
//        User user = currentUserService.getCurrentUser();
//
//        // 1. 단체 생성
//        Organization organization = request.toEntity(user);
//        organizationRepository.save(organization);
//
//        // 2. 기수 생성
//        OrganizationTerm term = request.toTermEntity(organization, user);
//        termRepository.save(term);
//
//        // TODO: 3. 생성자를 첫 기수의 STAFF로 등록
//
//        // TODO: 4. 첫 기수의 STAFF용 초대 코드 생성
//
//        // TODO: 5. 첫 기수의 MEMBER용 초애 코드 생성
//
//        // 응답 반환
//        return OrganizationCreateResponse.of(organization, term, termMember);
//    }

//    @Transactional(readOnly = true)
//    public OrganizationListResponse getMyOrganizations() {
//
//    }
}
