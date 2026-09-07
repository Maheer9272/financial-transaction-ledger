package com.maheer9272.LedgerCore.service;

import com.maheer9272.LedgerCore.dto.DepositRequestDto;
import com.maheer9272.LedgerCore.entity.*;
import com.maheer9272.LedgerCore.exception.IdempotencyKeyConflictException;
import com.maheer9272.LedgerCore.exception.ResourceDeniedException;
import com.maheer9272.LedgerCore.exception.UserNotActiveException;
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
public class TransactionServiceTest {

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

    /*
    Proves the complete happy path covering the validation passes
    Transaction is created, Two ledger entries are created. Ledger balances
    Customer balance increases. System balance decreases.Transaction completes
    Idempotency record is created
     */
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
        Account account = new Account(sourceUser);
        Account systemAccount = Account
                .createSystemAccount(new BigDecimal("100000.00"));

        //Tell mockito what CurrentUserResolver should return basically Stubbing
        when(currentUserResolver.resolve(authentication))
                .thenReturn(sourceUser);

        when(idempotencyRecordService.getHash(
                accountNumber,
                depositAmount
        )).thenReturn("test-request-hash");

        when(idempotencyRecordRepository.findByIdempotencyKey("Test-123"))
                .thenReturn(Optional.empty());

        when(accountRepository.findByAccountNumberAndUserIdForUpdate(
                eq(accountNumber),
                nullable(UUID.class)))
                .thenReturn(Optional.of(account)
                );

        when(accountRepository.findByAccountTypeForUpdate(AccountType.SYSTEM))
                .thenReturn(Optional.of(systemAccount));

        when(transactionRepository.save(any(FinancialTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(ledgerEntryRepository.save(any(LedgerEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(ledgerEntryRepository.sumAmountByTransactionAndEntryType(
                nullable(UUID.class),
                eq(LedgerEntryType.DEBIT)
        )).thenReturn(depositAmount);

        when(ledgerEntryRepository.sumAmountByTransactionAndEntryType(
                nullable(UUID.class),
                eq(LedgerEntryType.CREDIT)
        )).thenReturn(depositAmount);

        //Assert
        assertEquals(
                new BigDecimal("1000.00"),
                account.getBalance()
        );
        assertEquals(
                new BigDecimal("99000.00"),
                systemAccount.getBalance()
        );
        verify(transactionRepository).save(any(FinancialTransaction.class));
        verify(ledgerEntryRepository, times(2))
                .save(any(LedgerEntry.class));
        verify(idempotencyRecordRepository)
                .save(any(IdempotencyRecord.class));
    }

    /*
    User cant perform operation if user status is inactive
    */
    @Test
    void shouldRejectDepositWhenUserIsNotActive() {

        // Arrange
        User sourceUser = new User(
                "Test User",
                "test@example.com",
                "password"
        );
        sourceUser.suspend();

        when(currentUserResolver.resolve(authentication))
                .thenReturn(sourceUser);

        DepositRequestDto requestDto = new DepositRequestDto(
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
    }

    /*
    Can't deposit in another user's account
    */
    @Test
    void shouldRejectDepositWhenAccountDoesNotBelongToUser(){
        // Arrange
        User userA = new User(
                "Test User B",
                "testA@example.com",
                "password"
        );

        String accountNumberOfUserB = "12345679";
        Account systemAccount =
                Account.createSystemAccount(new BigDecimal("1000.00"));

        when(currentUserResolver.resolve(authentication))
                .thenReturn(userA);
        when(idempotencyRecordService.getHash(
                accountNumberOfUserB,
                new BigDecimal("1000.00")
        )).thenReturn("test-request-hash");

        when(idempotencyRecordRepository.findByIdempotencyKey("Test-123"))
                .thenReturn(Optional.empty());

        when(accountRepository.findByAccountTypeForUpdate(AccountType.SYSTEM))
                .thenReturn(Optional.of(systemAccount));

        when(accountRepository.findByAccountNumberAndUserIdForUpdate(
                eq(accountNumberOfUserB),
                nullable(UUID.class)))
                .thenReturn(Optional.empty()
                );

        DepositRequestDto requestDto = new DepositRequestDto(
                accountNumberOfUserB,
                new BigDecimal("1000.00"),
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
    }

    @Test
    void duplicateIdempotencyKeyForDeposit(){
        User user= new User(
                "Test User",
                "test@gmail.com",
                "test12345"
        );
        BigDecimal depositAmount = new BigDecimal("1000.00");
        String accountNumber = "12345678";
        String oldHash = "oldHash";
        DepositRequestDto requestDto = new DepositRequestDto(
                accountNumber,
                depositAmount,
                "Test Description"
        );
        FinancialTransaction depositTransaction = new FinancialTransaction(
                TransactionType.DEPOSIT,
                depositAmount,
                "Old deposit"
        );

        IdempotencyRecord idempotencyRecord = new IdempotencyRecord(
                "Idem123",
                depositTransaction,
                oldHash
        );

        when(currentUserResolver.resolve(authentication))
                .thenReturn(user);
        when(idempotencyRecordService.getHash(accountNumber,
                depositAmount))
                .thenReturn("newHash");
        when(idempotencyRecordRepository.findByIdempotencyKey("Idem123"))
                .thenReturn(Optional.of(idempotencyRecord)
                );

        assertThrows(
                IdempotencyKeyConflictException.class,
                ()-> transactionService.deposit(
                        requestDto,
                        authentication,
                        "Idem123"
                )
        );
    }
}



















