package io.weber.printer;

import io.weber.service.Transaction;

import java.util.List;

public interface AccountStatementPrinter {
    void print(List<Transaction> list);
}
