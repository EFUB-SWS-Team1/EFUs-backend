package com.efus.backend.domain.charge.repository;

import com.efus.backend.domain.charge.entity.Charge;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChargeRepository extends JpaRepository<Charge, Long> {
}
