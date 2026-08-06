package com.efus.backend.domain.funding.repository;

import com.efus.backend.domain.funding.entity.Funding;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FundingRepository extends JpaRepository<Funding, Long> {

    Optional<Funding> findByIdAndOrganizationTerm_Id(Long fundingId, Long termId);

    Page<Funding> findByOrganizationTerm_Id(Long termId, Pageable pageable);
}