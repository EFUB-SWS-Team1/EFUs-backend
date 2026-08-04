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
    // TODO: Transaction-Funding 연관관계 병합 후 주석 해제
//    List<Transaction> findAllByFunding_IdAndDeletedFalseOrderByTransactionDateDesc(Long fundingId);

    // TODO: Transaction-Funding 연관관계 병합 후 주석 해제
    // @Query("""
    //         select coalesce(sum(t.amount), 0)
    //         from Transaction t
    //         where t.funding.id = :fundingId
    //           and t.transactionType = :transactionType
    //           and t.deleted = false
    //         """)
    // Long sumAmountByFundingIdAndTransactionType(
    //         @Param("fundingId") Long fundingId,
    //         @Param("transactionType") TransactionType transactionType
    // );

    // TODO: Transaction-Funding 연관관계 병합 후 fundingId 필터 조건 추가
    // and (:fundingId is null or t.funding.id = :fundingId)
    @Query("""
            select t
            from Transaction t
            where t.term.id = :termId
              and (:includeDeleted = true or t.deleted = false)
              and (:fromDate is null or t.transactionDate >= :fromDate)
              and (:toDate is null or t.transactionDate <= :toDate)
              and (:transactionType is null or t.transactionType = :transactionType)
            """)
    Page<Transaction> findLedgerTransactions(
            @Param("termId") Long termId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("transactionType") TransactionType transactionType,
            @Param("includeDeleted") boolean includeDeleted,
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
        """)
    Long sumLedgerTransactionAmount(
            @Param("termId") Long termId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate,
            @Param("transactionType") TransactionType transactionType,
            @Param("includeDeleted") boolean includeDeleted
    );

}