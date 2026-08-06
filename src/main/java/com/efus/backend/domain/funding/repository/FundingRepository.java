package com.efus.backend.domain.funding.repository;

import com.efus.backend.domain.funding.entity.Funding;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface FundingRepository extends JpaRepository<Funding, Long> {

    Optional<Funding> findByIdAndOrganizationTerm_Id(Long fundingId, Long termId);

    Page<Funding> findByOrganizationTerm_Id(Long termId, Pageable pageable);

    // 해당 기수의 모든 행사 예산 합계 조회
    @Query("SELECT COALESCE(SUM(f.budgetAmount), 0) FROM Funding f WHERE f.organizationTerm.id = :termId")
    Long sumBudgetAmountByTermId(@Param("termId") Long termId);

    // 해당 기수의 전체 행사 개수 조회
    Long countByOrganizationTerm_Id(Long termId);
}