package io.weber.printer;

import io.weber.formatter.ListAccountStatementFormatter;
import io.weber.service.Transaction;
import io.weber.service.TransactionType;
import org.itsallcode.io.Capturable;
import org.itsallcode.junit.sysextensions.SystemOutGuard;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ExtendWith(SystemOutGuard.class)
@DisplayName("Printer test")
public class FormattedAccountStatementPrinterTest {
    @Mock
    private ListAccountStatementFormatter formatter;
    @InjectMocks
    private FormattedAccountStatementPrinter printer;

    @Test
    @DisplayName("should print account statement")
    void printAccountStatement(final Capturable stream){
        var accountId = UUID.fromString("a7dcc471-df3b-4fca-bd2f-8f2e17e78e41");
        when(formatter.format(generatedList(accountId))).thenReturn(generatedListOfString(accountId));

        stream.capture();
        var outPut =generatedOutput(accountId);
        printer.print(generatedList(accountId));
        Assertions.assertEquals(outPut, stream.getCapturedData());

        verify(formatter).format(generatedList(accountId));
        verifyNoMoreInteractions(formatter);
    }
    private String generatedOutput(UUID id){
        return id.toString() +
                "\r\nTransaction{date=2022-01-20, amount=100.00, type=DEPOSIT, accountBalance=100.00}\r\n" +
                "Transaction{date=2022-02-16, amount=-5.20, type=WITHDRAW, accountBalance=94.80}\r\n" +
                "Transaction{date=2022-03-01, amount=22.00, type=DEPOSIT, accountBalance=116.80}\r\n" +
                "Transaction{date=2022-04-30, amount=-52.90, type=WITHDRAW, accountBalance=63.90}\r\n";
    }
    private List<Transaction> generatedList(UUID id1){

        return List.of(new Transaction(id1, LocalDate.of(2022,1,20),new BigDecimal(100)
                        .setScale(2, RoundingMode.HALF_EVEN), TransactionType.DEPOSIT,new BigDecimal(100).setScale(2, RoundingMode.HALF_EVEN)),
                new Transaction(id1,LocalDate.of(2022,2,16), BigDecimal.valueOf(-5.2)
                        .setScale(2, RoundingMode.HALF_EVEN),TransactionType.WITHDRAW,new BigDecimal("94.8").setScale(2, RoundingMode.HALF_EVEN)),
                new Transaction(id1,LocalDate.of(2022,3,1),new BigDecimal(22)
                        .setScale(2, RoundingMode.HALF_EVEN),TransactionType.DEPOSIT,new BigDecimal("116.8").setScale(2, RoundingMode.HALF_EVEN)),
                new Transaction(id1,LocalDate.of(2022,4,30), BigDecimal.valueOf(-52.90)
                        .setScale(2, RoundingMode.HALF_EVEN),TransactionType.WITHDRAW,new BigDecimal("63.9").setScale(2, RoundingMode.HALF_EVEN)));
    }

    private List<String> generatedListOfString(UUID id){
        return List.of(id.toString(),"Transaction{date=2022-01-20, amount=100.00, type=DEPOSIT, accountBalance=100.00}",
                "Transaction{date=2022-02-16, amount=-5.20, type=WITHDRAW, accountBalance=94.80}",
                "Transaction{date=2022-03-01, amount=22.00, type=DEPOSIT, accountBalance=116.80}",
                "Transaction{date=2022-04-30, amount=-52.90, type=WITHDRAW, accountBalance=63.90}");
    }
}
