package com.efus.backend.domain.organization.repository;

import com.efus.backend.domain.organization.entity.Organization;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    // 유저가 기수에 속해있는 단체를 중복 없이 모두 조회
    @Query("SELECT DISTINCT o FROM Organization o " +
            "JOIN OrganizationTerm ot ON o.id = ot.organization.id " +
            "JOIN TermMember tm ON ot.id = tm.term.id " +
            "WHERE tm.user.id = :userId")
    List<Organization> findOrganizationsByUserId(@Param("userId") Long userId);
}
