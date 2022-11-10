package io.weber.printer;

import io.weber.formatter.AccountStatementFormatter;
import io.weber.service.Transaction;

import java.util.List;

public class FormattedAccountStatementPrinter implements AccountStatementPrinter {
    private final AccountStatementFormatter formatter;

    public FormattedAccountStatementPrinter(AccountStatementFormatter formatter) {
        this.formatter = formatter;
    }

    @Override
    public void print(List<Transaction> list) {
        var formattedStatementLine = formatter.format(list);
        formattedStatementLine.forEach(System.out::println);
    }
}
