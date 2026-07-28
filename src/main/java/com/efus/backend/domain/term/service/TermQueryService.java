package com.efus.backend.domain.term.service;

import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.entity.TermStatus;
import com.efus.backend.domain.term.repository.TermRepository;
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
}