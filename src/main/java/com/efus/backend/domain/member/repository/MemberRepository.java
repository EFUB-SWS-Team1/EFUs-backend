package com.efus.backend.domain.member.repository;

import com.efus.backend.domain.member.entity.TermMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<TermMember, Long> {
}
