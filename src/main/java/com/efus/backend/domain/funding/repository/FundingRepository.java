package com.efus.backend.domain.funding.repository;

import com.efus.backend.domain.funding.entity.Funding;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FundingRepository extends JpaRepository<Funding, Long> {
}
