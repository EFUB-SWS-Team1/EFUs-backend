package com.efus.backend.domain.charge.repository;

import com.efus.backend.domain.charge.dto.internal.LedgerChargeDto;
import com.efus.backend.domain.charge.entity.Charge;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChargeRepository extends JpaRepository<Charge, Long> {
    @Query("""
            select new com.efus.backend.domain.charge.dto.internal.LedgerChargeDto(
                c.id,
                c.title,
                c.dueDate,
                c.requestedAmount,
                coalesce(sum(case
                    when cm.paymentStatus = com.efus.backend.domain.charge.entity.ChargeMemberPaymentStatus.PAID
                    then cm.assignedAmount
                    else 0L
                end), 0L),
                f.id,
                f.name,
                c.memo,
                c.deleted,
                c.createdAt
            )
            from Charge c
            left join c.funding f
            left join ChargeMember cm on cm.charge = c
            where c.term.id = :termId
              and (:fromDate is null or c.dueDate >= :fromDate)
              and (:toDate is null or c.dueDate <= :toDate)
              and (:includeDeleted = true or c.deleted = false)
              and (:fundingId is null or f.id = :fundingId)
            group by c.id, c.title, c.dueDate, c.requestedAmount,
                     f.id, f.name, c.memo, c.deleted, c.createdAt
            order by c.dueDate desc, c.createdAt desc
            """)
    List<LedgerChargeDto> findLedgerCharges(
            @Param("termId") Long termId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("includeDeleted") boolean includeDeleted,
            @Param("fundingId") Long fundingId
    );
}
