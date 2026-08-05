package com.efus.backend.domain.charge.repository;

import com.efus.backend.domain.charge.entity.ChargeMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChargeMemberRepository extends JpaRepository<ChargeMember, Long> {
}
