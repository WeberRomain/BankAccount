package io.weber.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record Transaction(UUID accountId, LocalDate date, BigDecimal amount, TransactionType type,
                          BigDecimal accountBalance) {
}
