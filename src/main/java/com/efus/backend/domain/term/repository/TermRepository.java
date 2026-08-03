package com.efus.backend.domain.term.repository;

import com.efus.backend.domain.term.entity.OrganizationTerm;
import com.efus.backend.domain.term.entity.TermStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TermRepository extends JpaRepository<OrganizationTerm, Long> {
    // 특정 단체의 현재 활성 기수 1건 조회
    Optional<OrganizationTerm> findByOrganizationIdAndTermStatus(Long organizationId, TermStatus termStatus);

    // 특정 단체의 종료된 기수 중, 종료일 기준 가장 최근 기수 1건 조회
    Optional<OrganizationTerm> findTopByOrganizationIdAndTermStatusOrderByEndDateDesc(Long organizationId, TermStatus termStatus);

    // 단체에 특정 상태의 기수가 존재하는지 확인
    boolean existsByOrganizationIdAndTermStatus(Long organizationId, TermStatus termStatus);
}
