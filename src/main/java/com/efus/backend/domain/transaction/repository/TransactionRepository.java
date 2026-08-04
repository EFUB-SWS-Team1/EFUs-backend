package com.efus.backend.domain.transaction.repository;

import com.efus.backend.domain.transaction.entity.Transaction;
import com.efus.backend.domain.transaction.entity.TransactionType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    Optional<Transaction> findByIdAndTerm_Id(Long transactionId, Long termId);

    Optional<Transaction> findByIdAndTerm_IdAndDeletedFalse(Long transactionId, Long termId);

    List<Transaction> findAllByTerm_IdAndDeletedFalse(Long termId);

    List<Transaction> findAllByFunding_IdAndDeletedFalseOrderByTransactionDateDesc(Long fundingId);


    @Query("""
            select coalesce(sum(t.amount), 0)
            from Transaction t
            where t.term.id = :termId
              and t.transactionType = :transactionType
              and t.deleted = false
            """)
    Long sumAmountByTermIdAndTransactionType(
            @Param("termId") Long termId,
            @Param("transactionType") TransactionType transactionType
    );

     @Query("""
             select coalesce(sum(t.amount), 0)
             from Transaction t
             where t.funding.id = :fundingId
               and t.transactionType = :transactionType
               and t.deleted = false
             """)
     Long sumAmountByFundingIdAndTransactionType(
             @Param("fundingId") Long fundingId,
             @Param("transactionType") TransactionType transactionType
     );

    @Query("""
            select t
            from Transaction t
            where t.term.id = :termId
              and (:includeDeleted = true or t.deleted = false)
              and (:fromDate is null or t.transactionDate >= :fromDate)
              and (:toDate is null or t.transactionDate <= :toDate)
              and (:transactionType is null or t.transactionType = :transactionType)
              and (:fundingId is null or t.funding.id = :fundingId)
            """)
    Page<Transaction> findLedgerTransactions(
            @Param("termId") Long termId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("transactionType") TransactionType transactionType,
            @Param("includeDeleted") boolean includeDeleted,
            @Param("fundingId") Long fundingId,
            Pageable pageable
    );

    @Query("""
        select coalesce(sum(t.amount), 0)
        from Transaction t
        where t.term.id = :termId
          and (:includeDeleted = true or t.deleted = false)
          and (:fromDate is null or t.transactionDate >= :fromDate)
          and (:toDate is null or t.transactionDate <= :toDate)
          and t.transactionType = :transactionType
          and (:fundingId is null or t.funding.id = :fundingId)
        """)
    Long sumLedgerTransactionAmount(
            @Param("termId") Long termId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("transactionType") TransactionType transactionType,
            @Param("includeDeleted") boolean includeDeleted,
            @Param("fundingId") Long fundingId
            );

}