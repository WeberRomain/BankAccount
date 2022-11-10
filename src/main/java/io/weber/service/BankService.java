package io.weber.service;

import io.weber.exception.InvalidTransactionException;

import java.math.BigDecimal;
import java.util.UUID;

public interface BankService {

    Transaction deposit(UUID id, BigDecimal amount) throws InvalidTransactionException;
}
