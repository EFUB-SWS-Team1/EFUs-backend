package com.efus.backend.domain.receipt.service;

import com.efus.backend.domain.receipt.entity.Receipt;
import com.efus.backend.domain.receipt.repository.ReceiptRepository;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReceiptQueryService {

    private final ReceiptRepository receiptRepository;

    public Map<Long, Receipt> getReceiptMapByTransactionIds(List<Long> transactionIds) {
        if (transactionIds.isEmpty()) {
            return Map.of();
        }

        return receiptRepository.findAllByTransaction_IdIn(transactionIds)
                .stream()
                .collect(Collectors.toMap(
                        receipt -> receipt.getTransaction().getId(),
                        Function.identity()
                ));
    }
}