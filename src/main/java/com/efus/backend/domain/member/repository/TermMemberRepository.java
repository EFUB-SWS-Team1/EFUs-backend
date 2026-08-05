package com.efus.backend.domain.member.repository;

import com.efus.backend.domain.member.entity.TermMember;
import com.efus.backend.domain.member.entity.TermMemberRole;
import com.efus.backend.domain.member.entity.TermMemberStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TermMemberRepository extends JpaRepository<TermMember, Long> {

     // 중복 가입 방지
     boolean existsByTermIdAndUserId(Long termId, Long userId);

     // 로그인 User 소속, 권한 확인
     Optional<TermMember> findByTermIdAndUserId(
             Long termId,
             Long userId
     );

     // 해당 기수에 속한 특정 TermMember 조회
     Optional<TermMember> findByIdAndTermId(
             Long termMemberId,
             Long termId
     );

     // 특정 기수의 상태별 구성원 전체 조회
     List<TermMember> findAllByTermIdAndStatus(
             Long termId,
             TermMemberStatus status
     );

     List<TermMember> findAllByTermIdAndIdIn(
             Long termId,
             Collection<Long> termMemberIds
     );

     // User가 특정 단체의 어떤 기수에 속한 적 있는지 확인
    boolean existsByTerm_Organization_IdAndUser_Id(Long organizationId, Long userId);

    // 특정 기수의 총 멤버 수 계산
    long countByTermId(Long termId);

    @EntityGraph(attributePaths = "user")
    @Query(
            value = """
                    select tm
                    from TermMember tm
                    join tm.user u
                    where tm.term.id = :termId
                      and (:keyword is null or lower(u.name) like lower(concat('%', :keyword, '%')))
                      and (:role is null or tm.role = :role)
                    order by case when tm.role = com.efus.backend.domain.member.entity.TermMemberRole.STAFF then 0 else 1 end,
                             u.name asc,
                             tm.id asc
                    """,
            countQuery = """
                    select count(tm)
                    from TermMember tm
                    join tm.user u
                    where tm.term.id = :termId
                      and (:keyword is null or lower(u.name) like lower(concat('%', :keyword, '%')))
                      and (:role is null or tm.role = :role)
                    """
    )
    Page<TermMember> findTermMembers(
            @Param("termId") Long termId,
            @Param("keyword") String keyword,
            @Param("role") TermMemberRole role,
            Pageable pageable
    );
}
