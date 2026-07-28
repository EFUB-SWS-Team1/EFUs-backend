package com.efus.backend.domain.term.repository;

import com.efus.backend.domain.term.entity.OrganizationTerm;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TermRepository extends JpaRepository<OrganizationTerm, Long> {
}
