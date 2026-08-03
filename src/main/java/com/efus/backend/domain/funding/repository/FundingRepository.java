package com.efus.backend.domain.funding.repository;

import com.efus.backend.domain.funding.entity.Funding;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface FundingRepository extends JpaRepository<Funding, Long> {

    Optional<Funding> findByIdAndOrganizationTerm_Id(Long fundingId, Long termId);
}