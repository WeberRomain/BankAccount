package io.weber.service;

import io.weber.exception.InvalidTransactionException;
import io.weber.exception.NotFoundAccountException;
import io.weber.exception.OverdraftException;
import io.weber.printer.AccountStatementPrinter;
import io.weber.repository.AccountRepository;
import io.weber.repository.TransactionRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.UUID;

public class NoOverdraftAccountService implements BankService{

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    private final AccountStatementPrinter printer;

    public NoOverdraftAccountService(AccountRepository accountRepository, TransactionRepository transactionRepository, AccountStatementPrinter printer){
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.printer = printer;
    }

    @Override
    public Transaction deposit(UUID id, BigDecimal amount) throws NotFoundAccountException,InvalidTransactionException{
        validateInput(id,amount);
        var amountScale = amount.setScale(2, RoundingMode.HALF_EVEN);
        var balance = getLastBalance(id);
        return addTransaction(id,LocalDate.now(),amountScale,TransactionType.DEPOSIT,balance.add(amountScale));
    }

    @Override
    public Transaction withdraw(UUID id, BigDecimal amount) throws NotFoundAccountException
            ,InvalidTransactionException,OverdraftException {
        validateInput(id,amount);
        var amountScale = amount.setScale(2, RoundingMode.HALF_EVEN);
        var balance = getLastBalance(id);
        if(balance.equals(BigDecimal.ZERO)){
            throw new OverdraftException("Balance is empty");
        } else if (balance.subtract(amountScale).compareTo(BigDecimal.ZERO)<0) {
            throw new InvalidTransactionException("Balance can't be negative");
        }
        return addTransaction(id,LocalDate.now(),amountScale,TransactionType.WITHDRAW,balance.subtract(amountScale));
    }

    @Override
    public void printAccountStatement(UUID id) throws NotFoundAccountException{
        if(!accountRepository.ifAccountExist(id)){
            throw new NotFoundAccountException("Account id not found");
        }
        var list = transactionRepository.getAllTransactions(id);
        printer.print(list);
    }

    private void validateInput(UUID id, BigDecimal amount) throws NotFoundAccountException,InvalidTransactionException{
        if(!accountRepository.ifAccountExist(id)){
            throw new NotFoundAccountException("Account id not found");
        }
        if(amount.compareTo(BigDecimal.ZERO) <=0){
            throw new InvalidTransactionException("Amount can't be negative or zero");
        }
    }

    private BigDecimal getLastBalance(UUID id){
        var lastTransaction = transactionRepository.getLastTransaction(id);
        return lastTransaction.map(Transaction::accountBalance).orElse(BigDecimal.ZERO)
                .setScale(2, RoundingMode.HALF_EVEN);
    }

    private Transaction addTransaction(UUID id, LocalDate date, BigDecimal amount, TransactionType type, BigDecimal balance){
        return transactionRepository.addTransaction(new Transaction(id,date,amount,type,balance));
    }
}
