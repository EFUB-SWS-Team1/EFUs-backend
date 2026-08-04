package com.efus.backend.domain.receipt.repository;

import com.efus.backend.domain.receipt.entity.Receipt;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

    Optional<Receipt> findByTransaction_Id(Long transactionId);

    Optional<Receipt> findByTransaction_IdAndTransaction_Term_Id(
            Long transactionId,
            Long termId
    );

    boolean existsByTransaction_Id(Long transactionId);

    List<Receipt> findAllByTransaction_IdIn(List<Long> transactionIds);
}