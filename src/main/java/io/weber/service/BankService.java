package io.weber.service;

import java.math.BigDecimal;
import java.util.UUID;

public interface BankService {
    Transaction deposit(UUID id, BigDecimal amount);
    Transaction withdraw(UUID id, BigDecimal amount);
}
