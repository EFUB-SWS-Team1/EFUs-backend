package com.efus.backend.domain.transaction.repository;

import com.efus.backend.domain.transaction.entity.TransactionHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {

    Page<TransactionHistory> findAllByTransaction_IdOrderByChangedAtDesc(
            Long transactionId,
            Pageable pageable
    );
}