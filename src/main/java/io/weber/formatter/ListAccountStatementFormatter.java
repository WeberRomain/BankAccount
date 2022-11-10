package io.weber.formatter;

import io.weber.service.Transaction;

import java.util.List;
import java.util.stream.Stream;

public class ListAccountStatementFormatter implements AccountStatementFormatter {

    public List<String> format(List<Transaction> transactions) {
        return Stream.concat(
                transactions.stream().findFirst().stream().map(transaction -> transaction.accountId().toString()),
                transactions.stream().map(transaction -> "Transaction{" +
                        "date=" + transaction.date() +
                        ", amount=" + transaction.amount() +
                        ", type=" + transaction.type() +
                        ", accountBalance=" + transaction.accountBalance() +
                        "}")
        ).toList();
    }
}
