package com.efus.backend.domain.transaction.repository;

import com.efus.backend.domain.transaction.entity.Transaction;
import com.efus.backend.domain.transaction.entity.TransactionType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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
}