package com.efus.backend.domain.term.service;

import com.efus.backend.domain.term.dto.response.TermDetailResponse;
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

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TermQueryService {

    private final TermRepository termRepository;
    private final CurrentUserService currentUserService;

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

    public TermDetailResponse getTermDetail(Long termId) {
        User user = currentUserService.getCurrentUser();

        // 1. 기수 조회 (없으면 404 TERM_NOT_FOUND)[cite: 6]
        OrganizationTerm term = getTerm(termId); // 기존에 만들어둔 getTerm 재사용 가능

        // TODO 2. 권한 검증: 사용자가 '이 기수' 또는 '이 기수가 속한 단체의 다른 기수'에 가입한 이력이 있는지 확인[cite: 6]
        // (가입 이력이 아예 없으면 403 TERM_ACCESS_DENIED)[cite: 6]

        // TODO 3. 기수 구성원 수(memberCount) 집계[cite: 6]

        // TODO 4. 로그인 사용자가 '현재 조회 중인 기수'에 직접 가입했는지 확인
        // - 가입했다면 myTermMemberId, myRole 세팅[cite: 6]
        // - 가입하지 않고 다른 기수에만 가입한 상태라면 null 세팅[cite: 6]

        // 5. 뼈대 반환 (실제 구현 시 조립된 DTO 반환)
        return null;
    }
}