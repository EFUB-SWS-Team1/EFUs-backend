package com.efus.backend.domain.charge.repository;

import com.efus.backend.domain.charge.dto.internal.MemberChargeSummary;
import com.efus.backend.domain.charge.entity.ChargeMember;
import com.efus.backend.domain.charge.entity.ChargeMemberPaymentStatus;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChargeMemberRepository extends JpaRepository<ChargeMember, Long> {
    @Query("""
            select new com.efus.backend.domain.charge.dto.internal.MemberChargeSummary(
                coalesce(sum(case when cm.paymentStatus = com.efus.backend.domain.charge.entity.ChargeMemberPaymentStatus.PAID
                                  then cm.assignedAmount else 0L end), 0L),
                coalesce(sum(case when cm.paymentStatus = com.efus.backend.domain.charge.entity.ChargeMemberPaymentStatus.UNPAID
                                  then cm.assignedAmount else 0L end), 0L)
            )
            from ChargeMember cm
            where cm.termMember.id = :termMemberId
              and cm.charge.term.id = :termId
              and cm.charge.deleted = false
            """)
    MemberChargeSummary summarizeAmountsByTermMember(
            @Param("termId") Long termId,
            @Param("termMemberId") Long termMemberId
    );

    List<ChargeMember> findAllByChargeId(Long chargeId);
    List<ChargeMember> findAllByChargeIdAndPaymentStatus(
            Long chargeId, ChargeMemberPaymentStatus paymentStatus);
    List<ChargeMember> findAllByIdIn(List<Long> ids);
    boolean existsByChargeIdAndPaymentStatus(Long chargeId, ChargeMemberPaymentStatus paymentStatus);
    void deleteAllByChargeId(Long chargeId);

    @EntityGraph(attributePaths = {"termMember", "termMember.user"})
    @Query(value = """
            select cm
            from ChargeMember cm
            join cm.termMember tm
            join tm.user u
            where cm.charge.id = :chargeId
              and (:keyword is null or lower(u.name) like lower(concat('%', :keyword, '%')))
              and (:paymentStatus is null or cm.paymentStatus = :paymentStatus)
            order by u.name asc, tm.id asc
            """,
            countQuery = """
            select count(cm)
            from ChargeMember cm
            join cm.termMember tm
            join tm.user u
            where cm.charge.id = :chargeId
              and (:keyword is null or lower(u.name) like lower(concat('%', :keyword, '%')))
              and (:paymentStatus is null or cm.paymentStatus = :paymentStatus)
            """)
    Page<ChargeMember> findChargeMembers(
            @Param("chargeId") Long chargeId,
            @Param("keyword") String keyword,
            @Param("paymentStatus") ChargeMemberPaymentStatus paymentStatus,
            Pageable pageable
    );
}
