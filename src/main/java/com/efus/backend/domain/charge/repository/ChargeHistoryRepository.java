package com.efus.backend.domain.charge.repository;

import com.efus.backend.domain.charge.entity.ChargeHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChargeHistoryRepository extends JpaRepository<ChargeHistory, Long> {

    @EntityGraph(attributePaths = {"actorTermMember", "actorTermMember.user"})
    Page<ChargeHistory> findAllByCharge_IdOrderByChangedAtDesc(Long chargeId, Pageable pageable);
}
