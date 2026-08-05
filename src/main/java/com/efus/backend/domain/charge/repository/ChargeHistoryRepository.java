package com.efus.backend.domain.charge.repository;

import com.efus.backend.domain.charge.entity.ChargeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChargeHistoryRepository extends JpaRepository<ChargeHistory, Long> {
}
