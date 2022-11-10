package io.weber.repository;

import io.weber.service.Transaction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    Transaction addTransaction(Transaction transaction);
    Optional<Transaction> getLastTransaction(UUID id);
    List<Transaction> getAllTransactions(UUID id);

}
