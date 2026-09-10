package com.maheer9272.LedgerCore.service;

import com.maheer9272.LedgerCore.dto.*;
import com.maheer9272.LedgerCore.entity.*;
import com.maheer9272.LedgerCore.exception.*;
import com.maheer9272.LedgerCore.repository.AccountRepository;
import com.maheer9272.LedgerCore.repository.FinancialTransactionRepository;
import com.maheer9272.LedgerCore.repository.IdempotencyRecordRepository;
import com.maheer9272.LedgerCore.repository.LedgerEntryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock
    private FinancialTransactionRepository transactionRepository;
    @Mock
    private CurrentUserResolver currentUserResolver;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private LedgerEntryRepository ledgerEntryRepository;
    @Mock
    private IdempotencyRecordService idempotencyRecordService;
    @Mock
    private IdempotencyRecordRepository idempotencyRecordRepository;
    @Mock
    private Authentication authentication;
    @InjectMocks
    private TransactionService transactionService;

    @Test
    void shouldDepositSuccessfully() {

        // Arrange
        String accountNumber = "12345678";
        BigDecimal depositAmount = new BigDecimal("1000.00");
        User sourceUser = new User(
                "Test User",
                "test@example.com",
                "password"
        );

        Account userAccount = new Account(sourceUser);

        Account systemAccount =
                Account.createSystemAccount(
                        new BigDecimal("100000.00")
                );

        when(currentUserResolver.resolve(authentication))
                .thenReturn(sourceUser);

        when(idempotencyRecordService.getHash(
                accountNumber,
                depositAmount
        )).thenReturn("test-request-hash");
        when(idempotencyRecordRepository.findByIdempotencyKey(
                "Test-123"
        )).thenReturn(Optional.empty());
        when(accountRepository.findByAccountTypeForUpdate(
                AccountType.SYSTEM
        )).thenReturn(Optional.of(systemAccount));
        when(accountRepository.findByAccountNumberAndUserIdForUpdate(
                eq(accountNumber),
                nullable(UUID.class)
        )).thenReturn(Optional.of(userAccount));
        when(transactionRepository.saveAndFlush(
                any(FinancialTransaction.class)
        )).thenAnswer(invocation -> invocation.getArgument(0));
        when(ledgerEntryRepository.save(
                any(LedgerEntry.class)
        )).thenAnswer(invocation -> invocation.getArgument(0));
        DepositRequestDto requestDto =
                new DepositRequestDto(
                        accountNumber,
                        depositAmount,
                        "Test description"
                );

        // Act
        TransactionResponseDto result =
                transactionService.deposit(
                        requestDto,
                        authentication,
                        "Test-123"
                );

        // Assert
        assertEquals(
                new BigDecimal("1000.00"),
                userAccount.getBalance()
        );
        assertEquals(
                new BigDecimal("99000.00"),
                systemAccount.getBalance()
        );
        assertEquals(
                TransactionType.DEPOSIT,
                result.getTransactionType()
        );
        assertEquals(
                depositAmount,
                result.getAmount()
        );
        assertEquals(
                TransactionStatus.COMPLETED,
                result.getStatus()
        );

        verify(transactionRepository)
                .saveAndFlush(any(FinancialTransaction.class));
        verify(ledgerEntryRepository, times(2))
                .save(any(LedgerEntry.class));
        verify(idempotencyRecordRepository)
                .save(any(IdempotencyRecord.class));
    }


    @Test
    void shouldRejectDepositWhenUserIsNotActive() {

        // Arrange
        User user = new User(
                "Test User",
                "test@example.com",
                "password"
        );
        user.suspend();
        when(currentUserResolver.resolve(authentication))
                .thenReturn(user);

        DepositRequestDto requestDto =
                new DepositRequestDto(
                        "12345678",
                        new BigDecimal("1000.00"),
                        "Test deposit"
                );

        // Act & Assert
        assertThrows(
                UserNotActiveException.class,
                () -> transactionService.deposit(
                        requestDto,
                        authentication,
                        "Test-123"
                )
        );

        verifyNoInteractions(accountRepository);
        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(ledgerEntryRepository);
        verifyNoInteractions(idempotencyRecordRepository);
    }


    @Test
    void shouldRejectDepositWhenAccountDoesNotBelongToUser() {
        // Arrange
        String accountNumber = "12345679";
        BigDecimal depositAmount = new BigDecimal("1000.00");
        User user = new User(
                "Test User",
                "test@example.com",
                "password"
        );

        Account systemAccount =
                Account.createSystemAccount(
                        new BigDecimal("100000.00")
                );

        when(currentUserResolver.resolve(authentication))
                .thenReturn(user);
        when(idempotencyRecordService.getHash(
                accountNumber,
                depositAmount
        )).thenReturn("test-request-hash");
        when(idempotencyRecordRepository.findByIdempotencyKey(
                "Test-123"
        )).thenReturn(Optional.empty());
        when(accountRepository.findByAccountTypeForUpdate(
                AccountType.SYSTEM
        )).thenReturn(Optional.of(systemAccount));
        when(accountRepository.findByAccountNumberAndUserIdForUpdate(
                eq(accountNumber),
                nullable(UUID.class)
        )).thenReturn(Optional.empty());

        DepositRequestDto requestDto =
                new DepositRequestDto(
                        accountNumber,
                        depositAmount,
                        "Test deposit"
                );

        // Act & Assert
        assertThrows(
                ResourceDeniedException.class,
                () -> transactionService.deposit(
                        requestDto,
                        authentication,
                        "Test-123"
                )
        );

        verify(transactionRepository, never())
                .saveAndFlush(any(FinancialTransaction.class));
        verify(ledgerEntryRepository, never())
                .save(any(LedgerEntry.class));
        verify(idempotencyRecordRepository, never())
                .save(any(IdempotencyRecord.class));
    }


    @Test
    void duplicateIdempotencyKeyForDeposit() {
        // Arrange
        User user = new User(
                "Test User",
                "test@gmail.com",
                "test12345"
        );

        BigDecimal depositAmount =
                new BigDecimal("1000.00");

        String accountNumber = "12345678";
        String oldHash = "oldHash";

        DepositRequestDto requestDto =
                new DepositRequestDto(
                        accountNumber,
                        depositAmount,
                        "Test Description"
                );

        FinancialTransaction depositTransaction =
                new FinancialTransaction(
                        TransactionType.DEPOSIT,
                        depositAmount,
                        "Old deposit"
                );

        IdempotencyRecord idempotencyRecord =
                new IdempotencyRecord(
                        "Idem123",
                        depositTransaction,
                        oldHash
                );

        when(currentUserResolver.resolve(authentication))
                .thenReturn(user);
        when(idempotencyRecordService.getHash(
                accountNumber,
                depositAmount
        )).thenReturn("newHash");
        when(idempotencyRecordRepository.findByIdempotencyKey(
                "Idem123"
        )).thenReturn(Optional.of(idempotencyRecord));

        // Act & Assert
        assertThrows(
                IdempotencyKeyConflictException.class,
                () -> transactionService.deposit(
                        requestDto,
                        authentication,
                        "Idem123"
                )
        );

        verify(accountRepository, never())
                .findByAccountTypeForUpdate(AccountType.SYSTEM);
        verify(transactionRepository, never())
                .saveAndFlush(any(FinancialTransaction.class));
        verify(ledgerEntryRepository, never())
                .save(any(LedgerEntry.class));
        verify(idempotencyRecordRepository, never())
                .save(any(IdempotencyRecord.class));
    }


    @Test
    void shouldReturnDepositWhenIdempotencyKeyIsReused() {
        // Arrange
        User user = new User(
                "Test User",
                "test@gmail.com",
                "test12345"
        );

        BigDecimal depositAmount =
                new BigDecimal("1000.00");
        String accountNumber = "12345678";
        String oldHash = "oldHash";
        DepositRequestDto requestDto =
                new DepositRequestDto(
                        accountNumber,
                        depositAmount,
                        "Test Description"
                );
        FinancialTransaction depositTransaction =
                new FinancialTransaction(
                        TransactionType.DEPOSIT,
                        depositAmount,
                        "Old deposit"
                );
        IdempotencyRecord idempotencyRecord =
                new IdempotencyRecord(
                        "Idem123",
                        depositTransaction,
                        oldHash
                );

        when(currentUserResolver.resolve(authentication))
                .thenReturn(user);
        when(idempotencyRecordService.getHash(
                accountNumber,
                depositAmount
        )).thenReturn(oldHash);
        when(idempotencyRecordRepository.findByIdempotencyKey(
                "Idem123"
        )).thenReturn(Optional.of(idempotencyRecord));

        // Act
        TransactionResponseDto result =
                transactionService.deposit(
                        requestDto,
                        authentication,
                        "Idem123"
                );

        // Assert
        assertEquals(
                TransactionType.DEPOSIT,
                result.getTransactionType()
        );
        assertEquals(
                depositAmount,
                result.getAmount()
        );
        assertEquals(
                depositTransaction.getTransactionStatus(),
                result.getStatus()
        );
        assertEquals(
                "Old deposit",
                result.getMessage()
        );
        verify(transactionRepository, never())
                .saveAndFlush(any(FinancialTransaction.class));

        verify(ledgerEntryRepository, never())
                .save(any(LedgerEntry.class));
        verify(idempotencyRecordRepository, never())
                .save(any(IdempotencyRecord.class));
        verify(accountRepository, never())
                .findByAccountTypeForUpdate(AccountType.SYSTEM);
    }

    @Test
    void shouldWithdrawSuccessfully() {

        // Arrange
        String accountNumber = "12345678";
        BigDecimal withdrawalAmount =
                new BigDecimal("1000.00");
        User user = new User(
                "Test User",
                "test@example.com",
                "password"
        );
        Account userAccount =
                new Account(user);
        userAccount.credit(
                new BigDecimal("10000.00")
        );

        Account systemAccount =
                Account.createSystemAccount(
                        new BigDecimal("100000.00")
                );
        when(currentUserResolver.resolve(authentication))
                .thenReturn(user);
        when(idempotencyRecordService.getHash(
                accountNumber,
                withdrawalAmount
        )).thenReturn("test-request-hash");
        when(idempotencyRecordRepository.findByIdempotencyKey(
                "Test-123"
        )).thenReturn(Optional.empty());
        when(accountRepository.findByAccountTypeForUpdate(
                AccountType.SYSTEM
        )).thenReturn(Optional.of(systemAccount));
        when(accountRepository.findByAccountNumberAndUserIdForUpdate(
                eq(accountNumber),
                nullable(UUID.class)
        )).thenReturn(Optional.of(userAccount));
        when(transactionRepository.saveAndFlush(
                any(FinancialTransaction.class)
        )).thenAnswer(invocation -> invocation.getArgument(0));
        when(ledgerEntryRepository.save(
                any(LedgerEntry.class)
        )).thenAnswer(invocation -> invocation.getArgument(0));

        WithdrawalRequestDto requestDto =
                new WithdrawalRequestDto(
                        accountNumber,
                        withdrawalAmount,
                        "Test description"
                );

        // Act
        TransactionResponseDto result =
                transactionService.withdraw(
                        requestDto,
                        authentication,
                        "Test-123"
                );

        // Assert
        assertEquals(
                new BigDecimal("9000.00"),
                userAccount.getBalance()
        );
        assertEquals(
                new BigDecimal("101000.00"),
                systemAccount.getBalance()
        );
        assertEquals(
                TransactionType.WITHDRAWAL,
                result.getTransactionType()
        );
        assertEquals(
                withdrawalAmount,
                result.getAmount()
        );
        assertEquals(
                TransactionStatus.COMPLETED,
                result.getStatus()
        );
        verify(transactionRepository)
                .saveAndFlush(any(FinancialTransaction.class));
        verify(ledgerEntryRepository, times(2))
                .save(any(LedgerEntry.class));
        verify(idempotencyRecordRepository)
                .save(any(IdempotencyRecord.class));
    }


    @Test
    void withdrawalRejectedForInsufficientBalance() {

        // Arrange
        User user = new User(
                "Withdrawal test",
                "withdraw@example.com",
                "pass123"
        );
        Account userAccount =
                new Account(user);
        String accountNumber = "87654321";
        BigDecimal withdrawalAmount =
                new BigDecimal("25000.00");
        Account systemAccount =
                Account.createSystemAccount(
                        new BigDecimal("100000.00")
                );

        when(currentUserResolver.resolve(authentication))
                .thenReturn(user);
        when(idempotencyRecordService.getHash(
                accountNumber,
                withdrawalAmount
        )).thenReturn("new-hash");
        when(idempotencyRecordRepository.findByIdempotencyKey(
                "Test-123"
        )).thenReturn(Optional.empty());
        when(accountRepository.findByAccountTypeForUpdate(
                AccountType.SYSTEM
        )).thenReturn(Optional.of(systemAccount));
        when(accountRepository.findByAccountNumberAndUserIdForUpdate(
                eq(accountNumber),
                nullable(UUID.class)
        )).thenReturn(Optional.of(userAccount));

        WithdrawalRequestDto requestDto =
                new WithdrawalRequestDto(
                        accountNumber,
                        withdrawalAmount,
                        "Test description"
                );

        // Act & Assert
        assertThrows(
                InsufficientBalanceException.class,
                () -> transactionService.withdraw(
                        requestDto,
                        authentication,
                        "Test-123"
                )
        );

        verify(transactionRepository)
                .saveAndFlush(any(FinancialTransaction.class));
        verify(ledgerEntryRepository, times(2))
                .save(any(LedgerEntry.class));
        verify(idempotencyRecordRepository)
                .save(any(IdempotencyRecord.class));
        assertEquals(
                BigDecimal.ZERO,
                userAccount.getBalance()
        );
        assertEquals(
                new BigDecimal("100000.00"),
                systemAccount.getBalance()
        );
    }

    @Test
    void shouldTransferSuccessfully() {
        // Arrange
        String fromAccountNumber = "12345678";
        String toAccountNumber = "87654321";
        BigDecimal transferAmount =
                new BigDecimal("1000.00");
        User sourceUser = new User(
                "Source User",
                "source@example.com",
                "pass123"
        );
        User destinationUser = new User(
                "Destination User",
                "destination@example.com",
                "pass123"
        );

        Account sourceAccount =
                new Account(sourceUser);

        Account destinationAccount =
                new Account(destinationUser);

        sourceAccount.credit(
                new BigDecimal("5000.00")
        );
        destinationAccount.credit(
                new BigDecimal("5000.00")
        );

        when(currentUserResolver.resolve(authentication))
                .thenReturn(sourceUser);
        when(idempotencyRecordService.getHash(
                fromAccountNumber,
                toAccountNumber,
                transferAmount
        )).thenReturn("new-hash");
        when(idempotencyRecordRepository.findByIdempotencyKey(
                "Test-123"
        )).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumberAndUserIdForUpdate(
                eq(fromAccountNumber),
                nullable(UUID.class)
        )).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumberForUpdate(
                eq(toAccountNumber)
        )).thenReturn(Optional.of(destinationAccount));
        when(transactionRepository.saveAndFlush(
                any(FinancialTransaction.class)
        )).thenAnswer(invocation -> invocation.getArgument(0));
        when(ledgerEntryRepository.save(
                any(LedgerEntry.class)
        )).thenAnswer(invocation -> invocation.getArgument(0));

        TransferRequestDto requestDto =
                new TransferRequestDto(
                        fromAccountNumber,
                        toAccountNumber,
                        transferAmount,
                        "Transfer Test"
                );

        // Act
        TransferResponseDto result =
                transactionService.transfer(
                        requestDto,
                        authentication,
                        "Test-123"
                );

        // Assert
        assertEquals(
                new BigDecimal("4000.00"),
                sourceAccount.getBalance()
        );
        assertEquals(
                new BigDecimal("6000.00"),
                destinationAccount.getBalance()
        );
        assertEquals(
                TransactionType.TRANSFER,
                result.getTransactionType()
        );
        assertEquals(
                transferAmount,
                result.getAmount()
        );
        assertEquals(
                TransactionStatus.COMPLETED,
                result.getStatus()
        );
        verify(transactionRepository)
                .saveAndFlush(any(FinancialTransaction.class));
        verify(ledgerEntryRepository, times(2))
                .save(any(LedgerEntry.class));
        verify(idempotencyRecordRepository)
                .save(any(IdempotencyRecord.class));
    }


    @Test
    void rejectSameAccountTransfer() {

        // Arrange
        String accountNumber = "12345678";
        BigDecimal transferAmount =
                new BigDecimal("1000.00");
        User user = new User(
                "Test User",
                "test@gmail.com",
                "pass123"
        );

        when(currentUserResolver.resolve(authentication))
                .thenReturn(user);

        TransferRequestDto requestDto =
                new TransferRequestDto(
                        accountNumber,
                        accountNumber,
                        transferAmount,
                        "Transfer test"
                );

        // Act & Assert
        assertThrows(
                InvalidTransactionRequestException.class,
                () -> transactionService.transfer(
                        requestDto,
                        authentication,
                        "Test-123"
                )
        );

        verifyNoInteractions(accountRepository);
        verifyNoInteractions(idempotencyRecordService);
        verifyNoInteractions(idempotencyRecordRepository);
        verifyNoInteractions(transactionRepository);
        verifyNoInteractions(ledgerEntryRepository);
    }


    @Test
    void transferWithInsufficientBalanceRejected() {

        // Arrange
        String fromAccountNumber = "12345678";
        String toAccountNumber = "87654321";
        BigDecimal transferAmount =
                new BigDecimal("60000.00");
        User sourceUser = new User(
                "Source User",
                "source@example.com",
                "pass123"
        );
        User destinationUser = new User(
                "Destination User",
                "destination@example.com",
                "pass123"
        );

        Account sourceAccount =
                new Account(sourceUser);

        Account destinationAccount =
                new Account(destinationUser);

        sourceAccount.credit(
                new BigDecimal("5000.00")
        );

        destinationAccount.credit(
                new BigDecimal("5000.00")
        );

        when(currentUserResolver.resolve(authentication))
                .thenReturn(sourceUser);
        when(idempotencyRecordService.getHash(
                fromAccountNumber,
                toAccountNumber,
                transferAmount
        )).thenReturn("new-hash");
        when(idempotencyRecordRepository.findByIdempotencyKey(
                "Test-123"
        )).thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumberAndUserIdForUpdate(
                eq(fromAccountNumber),
                nullable(UUID.class)
        )).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumberForUpdate(
                eq(toAccountNumber)
        )).thenReturn(Optional.of(destinationAccount));
        TransferRequestDto requestDto =
                new TransferRequestDto(
                        fromAccountNumber,
                        toAccountNumber,
                        transferAmount,
                        "Transfer Test"
                );

        // Act & Assert
        assertThrows(
                InsufficientBalanceException.class,
                () -> transactionService.transfer(
                        requestDto,
                        authentication,
                        "Test-123"
                )
        );
        verify(transactionRepository)
                .saveAndFlush(any(FinancialTransaction.class));
        verify(ledgerEntryRepository, times(2))
                .save(any(LedgerEntry.class));
        verify(idempotencyRecordRepository)
                .save(any(IdempotencyRecord.class));
        assertEquals(
                new BigDecimal("5000.00"),
                sourceAccount.getBalance()
        );
        assertEquals(
                new BigDecimal("5000.00"),
                destinationAccount.getBalance()
        );
    }
}