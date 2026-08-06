package com.efus.backend.domain.funding.dto.response;

import com.efus.backend.domain.funding.entity.Funding;
import com.efus.backend.domain.transaction.entity.TransactionType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
 import com.efus.backend.domain.transaction.entity.Transaction;

public record FundingDetailResponse(
        Long fundingId,
        Long termId,
        CreatedByResponse createdBy,
        String name,
        Long budgetAmount,
        Long spentAmount,
        Long remainingAmount,
        Long overBudgetAmount,
        Integer progressRate,
        String status,
        Integer participantCount,
        LocalDate startDate,
        LocalDate endDate,
        List<FundingTransactionResponse> transactions,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {

    public static FundingDetailResponse of(
            Funding funding,
            Long spentAmount,
            List<FundingTransactionResponse> transactions
    ) {
        Long budgetAmount = funding.getBudgetAmount();
        Long remainingAmount = budgetAmount - spentAmount;
        Long overBudgetAmount = Math.max(spentAmount - budgetAmount, 0L);
        Integer progressRate = Math.toIntExact(Math.min((spentAmount * 100) / budgetAmount, 100));

        return new FundingDetailResponse(
                funding.getId(),
                funding.getOrganizationTerm().getId(),
                CreatedByResponse.from(funding),
                funding.getName(),
                budgetAmount,
                spentAmount,
                remainingAmount,
                overBudgetAmount,
                progressRate,
                resolveStatus(funding, spentAmount),
                funding.getParticipantCount(),
                funding.getStartDate(),
                funding.getEndDate(),
                transactions,
                funding.getCreatedAt(),
                funding.getModifiedAt()
        );
    }

    private static String resolveStatus(Funding funding, Long spentAmount) {
        if (spentAmount > funding.getBudgetAmount()) {
            return "OVER_BUDGET";
        }

        if (spentAmount * 100 >= funding.getBudgetAmount() * 80) {
            return "WARNING";
        }

        LocalDate today = LocalDate.now();

        if (today.isBefore(funding.getStartDate())) {
            return "UPCOMING";
        }

        if (today.isAfter(funding.getEndDate())) {
            return "ENDED";
        }

        return "IN_PROGRESS";
    }

    public record CreatedByResponse(
            Long termMemberId,
            String name,
            String profileImageUrl
    ) {

        public static CreatedByResponse from(Funding funding) {
            return new CreatedByResponse(
                    funding.getCreatedByTermMember().getId(),
                    funding.getCreatedByTermMember().getUser().getName(),
                    funding.getCreatedByTermMember().getUser().getProfileImageUrl()
            );
        }
    }

    public record FundingTransactionResponse(
            Long transactionId,
            LocalDate transactionDate,
            String title,
            TransactionType transactionType,
            Long amount
    ) {

         public static FundingTransactionResponse from(Transaction transaction) {
             return new FundingTransactionResponse(
                     transaction.getId(),
                     transaction.getTransactionDate(),
                     transaction.getTitle(),
                     transaction.getTransactionType(),
                     transaction.getAmount()
             );
         }
    }
}