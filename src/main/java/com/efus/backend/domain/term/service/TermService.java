package com.efus.backend.domain.term.service;

import com.efus.backend.domain.term.dto.request.TermCreateRequest;
import com.efus.backend.domain.term.dto.response.TermCreateResponse;
import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.repository.TermRepository;
import com.efus.backend.domain.user.entity.User;
import com.efus.backend.domain.user.service.CurrentUserService;
// import com.efus.backend.domain.organization.service.OrganizationQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TermService {

    private final CurrentUserService currentUserService;
    private final TermRepository termRepository;
    // private final OrganizationQueryService organizationQueryService;
    // private final TermMemberRepository termMemberRepository;

    @Transactional
    public TermCreateResponse createTerm(Long organizationId, TermCreateRequest request) {
        User user = currentUserService.getCurrentUser();

        // TODO: 단체 엔티티 조회 (OrganizationQueryService 등 활용)

        // TODO 1: 권한 검증 - 해당 단체의 가장 최근 기수에서 STAFF였던 사용자만 요청 가능[cite: 4]
        // (권한이 없으면 403 TERM_CREATION_FORBIDDEN 에러 발생)[cite: 4]

        // TODO 2: 상태 검증 - 단체에 활성 기수가 존재하는지 확인[cite: 4]
        // (존재하면 409 ACTIVE_TERM_ALREADY_EXISTS 에러 발생)[cite: 4]

        // 3. 새로운 OrganizationTerm 생성[cite: 4]
        // OrganizationTerm term = request.toEntity(organization, user);
        // termRepository.save(term);

        // TODO 4: 요청자를 새 기수의 STAFF로 등록[cite: 4]

        // TODO 5 & 6: 새 기수의 STAFF용 / MEMBER용 초대 코드 생성[cite: 4]

        // 7. 응답 반환 (뼈대 구조)
        // return TermCreateResponse.of(term, organization, termMember.getId(), "STAFF");
        return null;
    }
}