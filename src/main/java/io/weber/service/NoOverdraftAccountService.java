package io.weber.service;

import io.weber.exception.InvalidTransactionException;
import io.weber.exception.NotFoundAccountException;
import io.weber.exception.OverdraftException;
import io.weber.repository.AccountRepository;
import io.weber.repository.TransactionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

public class NoOverdraftAccountService implements BankService{

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public NoOverdraftAccountService(AccountRepository accountRepository, TransactionRepository transactionRepository){
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    @Override
    public Transaction deposit(UUID id, BigDecimal amount){
        if(!accountRepository.ifAccountExist(id)){
            throw new NotFoundAccountException("Account id not found");
        }
        if(amount.compareTo(BigDecimal.ZERO) <=0){
            throw new InvalidTransactionException("Amount can't be negative or zero");
        }
        var amountScale = amount.setScale(2, RoundingMode.HALF_EVEN);
        var lastTransaction = transactionRepository.getLastTransaction(id);
        var balance = lastTransaction.map(Transaction::accountBalance).orElse(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_EVEN);
        return transactionRepository.addTransaction(new Transaction(id,LocalDate.now(),amountScale,TransactionType.DEPOSIT,balance.add(amountScale)));
    }

    @Override
    public Transaction withdraw(UUID id, BigDecimal amount) {
        if(!accountRepository.ifAccountExist(id)){
            throw new NotFoundAccountException("Account id not found");
        }
        if(amount.compareTo(BigDecimal.ZERO) <=0){
            throw new InvalidTransactionException("Amount can't be negative or zero");
        }
        var amountScale = amount.setScale(2, RoundingMode.HALF_EVEN);
        var lastTransaction = transactionRepository.getLastTransaction(id);
        var balance = lastTransaction.map(Transaction::accountBalance).orElse(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_EVEN);
        if(balance.equals(BigDecimal.ZERO)){
            throw new OverdraftException("Balance is empty");
        } else if (balance.subtract(amountScale).compareTo(BigDecimal.ZERO)<0) {
            throw new InvalidTransactionException("Balance can't be negative");
        }
        return transactionRepository.addTransaction(new Transaction(id,LocalDate.now(),amountScale,TransactionType.WITHDRAW,balance.subtract(amountScale)));
    }
}
