package com.efus.backend.domain.receipt.service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class ReceiptAmountParser {

    private static final Pattern AMOUNT_PATTERN = Pattern.compile("(\\d{1,3}(,\\d{3})+|\\d+)");

    public Optional<Long> parseAmount(List<String> lines) {
        return lines.stream()
                .filter(this::isLikelyTotalLine)
                .flatMap(line -> extractAmounts(line).stream())
                .max(Comparator.naturalOrder())
                .or(() -> lines.stream()
                        .flatMap(line -> extractAmounts(line).stream())
                        .max(Comparator.naturalOrder()));
    }

    private boolean isLikelyTotalLine(String line) {
        String normalized = line.toLowerCase();

        return normalized.contains("합계")
                || normalized.contains("총액")
                || normalized.contains("총 결제")
                || normalized.contains("결제금액")
                || normalized.contains("받을금액")
                || normalized.contains("total")
                || normalized.contains("amount");
    }

    private List<Long> extractAmounts(String line) {
        Matcher matcher = AMOUNT_PATTERN.matcher(line);

        return matcher.results()
                .map(match -> match.group().replace(",", ""))
                .map(Long::parseLong)
                .filter(amount -> amount > 0)
                .toList();
    }
}