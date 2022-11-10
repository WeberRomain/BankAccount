package io.weber.formatter;

import io.weber.service.Transaction;

import java.util.List;

public interface AccountStatementFormatter {
    List<String> format(List<Transaction> transactions);
}
