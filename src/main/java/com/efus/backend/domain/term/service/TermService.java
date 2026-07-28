package com.efus.backend.domain.term.service;

import com.efus.backend.domain.member.repository.TermMemberRepository;
import com.efus.backend.domain.term.dto.request.TermCreateRequest;
import com.efus.backend.domain.term.dto.response.TermCreateResponse;
import com.efus.backend.domain.term.dto.response.TermListResponse;
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
//    private final CurrentUserService currentUserService;
//    private final TermRepository termRepository;
//    private final OrganizationQueryService organizationQueryService;
    private final TermMemberRepository termMemberRepository;

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

    public TermListResponse getTermList(Long organizationId) {
        User user = currentUserService.getCurrentUser();

        // TODO 1. 단체 조회 및 예외 처리 (404 ORGANIZATION_NOT_FOUND)[cite: 5]

        // TODO 2. 권한 검증: 로그인한 사용자가 해당 단체의 기수에 하나라도 가입한 이력이 있는지 확인[cite: 5]
        // (가입 이력이 없으면 403 ORGANIZATION_ACCESS_DENIED)[cite: 5]

        // TODO 3. 단체에 속한 전체 기수 목록 조회 (최근 기수부터 정렬하여 가져오기)[cite: 5]

        // TODO 4. 조회한 기수 목록(Entity)을 stream().map()을 사용하여 DTO(TermDetailDto)로 변환
        // 변환 시 유의사항:
        // - 해당 기수의 memberCount 집계하여 세팅[cite: 5]
        // - 사용자가 해당 기수에 가입했는지 확인 후 myRole 세팅 (가입하지 않았으면 null)[cite: 5]

        // 5. 뼈대 반환
        return null;
    }
}