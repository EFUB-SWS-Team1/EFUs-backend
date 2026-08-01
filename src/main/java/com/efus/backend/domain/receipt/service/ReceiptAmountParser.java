package com.efus.backend.domain.receipt.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ReceiptAmountParser {

    private static final Long MIN_AMOUNT = 100L;
    private static final Long MAX_AMOUNT = 10_000_000L;
    private static final Pattern AMOUNT_PATTERN = Pattern.compile("\\d{1,3}(,\\d{3})+");

    public Optional<Long> parseAmount(List<String> lines) {
        List<Long> candidates = lines.stream()
                .filter(line -> !isExcludedLine(line))
                .flatMap(line -> extractAmounts(line).stream())
                .toList();

        if (candidates.isEmpty()) {
            return Optional.empty();
        }

        Map<Long, Long> frequencyByAmount = candidates.stream()
                .collect(Collectors.groupingBy(
                        amount -> amount,
                        Collectors.counting()
                ));

        return frequencyByAmount.entrySet().stream()
                .max(
                        Comparator
                                .<Map.Entry<Long, Long>>comparingLong(Map.Entry::getValue)
                                .thenComparingLong(Map.Entry::getKey)
                )
                .map(Map.Entry::getKey);
    }

    private List<Long> extractAmounts(String line) {
        Matcher matcher = AMOUNT_PATTERN.matcher(line);

        return matcher.results()
                .map(match -> match.group().replace(",", ""))
                .map(Long::parseLong)
                .filter(this::isReasonableAmount)
                .toList();
    }

    private boolean isExcludedLine(String line) {
        String normalized = line.toLowerCase();

        return normalized.contains("no:")
                || normalized.contains("gs25")
                || normalized.contains("tel")
                || normalized.contains("사업자")
                || normalized.contains("카드번호")
                || normalized.contains("승인번호");
    }

    private boolean isReasonableAmount(Long amount) {
        return amount >= MIN_AMOUNT && amount <= MAX_AMOUNT;
    }
}