package io.weber.service;

import io.weber.exception.InvalidTransactionException;
import io.weber.exception.NotFoundAccountException;
import io.weber.exception.OverdraftException;
import io.weber.repository.AccountRepository;
import io.weber.repository.TransactionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Bank Service test")
public class NoOverdraftAccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @InjectMocks
    private NoOverdraftAccountService service;

    @Nested
    @DisplayName("when making a deposit")
    class Deposit {

        @Test
        @DisplayName("should work with no initial balance on a known account")
        void workWithoutInitialBalance() throws NotFoundAccountException, InvalidTransactionException {
            var accountId = UUID.randomUUID();
            var amount = new BigDecimal(150).setScale(2, RoundingMode.HALF_EVEN);
            var newTransaction = new Transaction(accountId, LocalDate.now(),amount,
                    TransactionType.DEPOSIT, new BigDecimal(150).setScale(2, RoundingMode.HALF_EVEN));
            when(accountRepository.ifAccountExist(accountId)).thenReturn(true);
            when(transactionRepository.getLastTransaction(accountId)).thenReturn(Optional.empty());
            when(transactionRepository.addTransaction(newTransaction)).thenReturn(newTransaction);

            var item = service.deposit(accountId, amount);
            assertEquals(item, newTransaction);

            final var orderVerifier = inOrder(accountRepository, transactionRepository);
            orderVerifier.verify(accountRepository).ifAccountExist(accountId);
            orderVerifier.verify(transactionRepository).getLastTransaction(accountId);
            orderVerifier.verify(transactionRepository).addTransaction(newTransaction);
            orderVerifier.verifyNoMoreInteractions();
        }

        @Test
        @DisplayName("should work with an initial balance on a known account")
        void workWithInitialBalance() throws NotFoundAccountException, InvalidTransactionException {
            var accountId = UUID.randomUUID();
            var amount = new BigDecimal(150).setScale(2, RoundingMode.HALF_EVEN);
            var lastTransaction = new Transaction(accountId, LocalDate.now(),
                    new BigDecimal(100).setScale(2, RoundingMode.HALF_EVEN),
                    TransactionType.DEPOSIT, new BigDecimal(100).setScale(2, RoundingMode.HALF_EVEN));
            var newTransaction = new Transaction(accountId, LocalDate.now(),amount, TransactionType.DEPOSIT,
                    new BigDecimal(250).setScale(2, RoundingMode.HALF_EVEN));
            when(accountRepository.ifAccountExist(accountId)).thenReturn(true);
            when(transactionRepository.getLastTransaction(accountId)).thenReturn(Optional.of(lastTransaction));
            when(transactionRepository.addTransaction(newTransaction)).thenReturn(newTransaction);

            var item = service.deposit(accountId, amount);
            assertEquals(item, newTransaction);

            final var orderVerifier = inOrder(accountRepository, transactionRepository);
            orderVerifier.verify(accountRepository).ifAccountExist(accountId);
            orderVerifier.verify(transactionRepository).getLastTransaction(accountId);
            orderVerifier.verify(transactionRepository).addTransaction(newTransaction);
            orderVerifier.verifyNoMoreInteractions();
        }

        @Test
        @DisplayName("should not work with an unknown account")
        void throwNotFoundAccountException() {
            var accountId = UUID.randomUUID();
            var amount = new BigDecimal(150);
            when(accountRepository.ifAccountExist(accountId)).thenReturn(false);

            assertThrows(NotFoundAccountException.class,
                    () -> service.deposit(accountId, amount));

            verify(accountRepository).ifAccountExist(accountId);
            verifyNoMoreInteractions(accountRepository);
        }

        @Test
        @DisplayName("should not work with a negative amount on a known account")
        void throwInvalidTransactionExceptionWithNegativeAmount() {
            var accountId = UUID.randomUUID();
            var amount = new BigDecimal(-1);
            when(accountRepository.ifAccountExist(accountId)).thenReturn(true);

            assertThrows(InvalidTransactionException.class,
                    () -> service.deposit(accountId, amount));

            verify(accountRepository).ifAccountExist(accountId);
            verifyNoMoreInteractions(accountRepository);
        }
    }
    @Nested
    @DisplayName("when making a withdraw")
    class Withdraw {

        @Test
        @DisplayName("should work with an initial balance on a known account")
        void workWithInitialBalance() throws NotFoundAccountException, OverdraftException, InvalidTransactionException {
            var accountId = UUID.randomUUID();
            var amount = new BigDecimal(150).setScale(2, RoundingMode.HALF_EVEN);
            var lastTransaction = new Transaction(accountId, LocalDate.now(),
                    new BigDecimal(250).setScale(2, RoundingMode.HALF_EVEN),
                    TransactionType.DEPOSIT, new BigDecimal(250).setScale(2, RoundingMode.HALF_EVEN));
            var newTransaction = new Transaction(accountId, LocalDate.now(),amount,
                    TransactionType.WITHDRAW, new BigDecimal(100).setScale(2, RoundingMode.HALF_EVEN));
            when(accountRepository.ifAccountExist(accountId)).thenReturn(true);
            when(transactionRepository.getLastTransaction(accountId)).thenReturn(Optional.of(lastTransaction));
            when(transactionRepository.addTransaction(newTransaction)).thenReturn(newTransaction);

            var item = service.withdraw(accountId, amount);
            assertEquals(item, newTransaction);

            final var orderVerifier = inOrder(accountRepository, transactionRepository);
            orderVerifier.verify(accountRepository).ifAccountExist(accountId);
            orderVerifier.verify(transactionRepository).getLastTransaction(accountId);
            orderVerifier.verify(transactionRepository).addTransaction(newTransaction);
            orderVerifier.verifyNoMoreInteractions();
        }

        @Test
        @DisplayName("should not work with no initial balance on a known account")
        void throwInvalidTransactionExceptionWithoutInitialBalance() {
            var accountId = UUID.randomUUID();
            var amount = new BigDecimal(150);
            when(accountRepository.ifAccountExist(accountId)).thenReturn(true);
            when(transactionRepository.getLastTransaction(accountId)).thenReturn(Optional.empty());

            assertThrows(InvalidTransactionException.class,
                    () -> service.withdraw(accountId, amount));

            final var orderVerifier = inOrder(accountRepository, transactionRepository);
            orderVerifier.verify(accountRepository).ifAccountExist(accountId);
            orderVerifier.verify(transactionRepository).getLastTransaction(accountId);
            orderVerifier.verifyNoMoreInteractions();
        }

        @Test
        @DisplayName("should not work when overdraft on a known account")
        void throwOverdraftExceptionWhenAccountIsOverdraft() {
            var accountId = UUID.randomUUID();
            var amount = new BigDecimal(300);
            var lastTransaction = new Transaction(accountId, LocalDate.now(), new BigDecimal(250), TransactionType.DEPOSIT, new BigDecimal(250));
            when(accountRepository.ifAccountExist(accountId)).thenReturn(true);
            when(transactionRepository.getLastTransaction(accountId)).thenReturn(Optional.of(lastTransaction));


            assertThrows(InvalidTransactionException.class,
                    () -> service.withdraw(accountId, amount));

            final var orderVerifier = inOrder(accountRepository, transactionRepository);
            orderVerifier.verify(accountRepository).ifAccountExist(accountId);
            orderVerifier.verify(transactionRepository).getLastTransaction(accountId);
            orderVerifier.verifyNoMoreInteractions();
        }

        @Test
        @DisplayName("should not work with an unknown account")
        void throwNotFoundAccountException() {
            var accountId = UUID.randomUUID();
            var amount = new BigDecimal(150);
            when(accountRepository.ifAccountExist(accountId)).thenReturn(false);

            assertThrows(NotFoundAccountException.class,
                    () -> service.deposit(accountId, amount));

            verify(accountRepository).ifAccountExist(accountId);
            verifyNoMoreInteractions(accountRepository);
        }

        @Test
        @DisplayName("should not work with a negative amount on a known account")
        void throwInvalidTransactionExceptionWithNegativeValue() {
            var accountId = UUID.randomUUID();
            var amount = new BigDecimal(-1);
            when(accountRepository.ifAccountExist(accountId)).thenReturn(true);

            assertThrows(InvalidTransactionException.class,
                    () -> service.deposit(accountId, amount));

            verify(accountRepository).ifAccountExist(accountId);
            verifyNoMoreInteractions(accountRepository);
        }
    }
    }
