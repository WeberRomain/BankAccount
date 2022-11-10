package io.weber.service;

import io.weber.exception.InvalidTransactionException;
import io.weber.exception.NotFoundAccountException;
import io.weber.exception.OverdraftException;

import java.math.BigDecimal;
import java.util.UUID;

public interface BankService {
    Transaction deposit(UUID id, BigDecimal amount) throws NotFoundAccountException, InvalidTransactionException;
    Transaction withdraw(UUID id, BigDecimal amount) throws NotFoundAccountException
            ,InvalidTransactionException, OverdraftException;
    void printAccountStatement(UUID id) throws NotFoundAccountException;


}
