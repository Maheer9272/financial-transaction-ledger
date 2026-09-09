package com.maheer9272.LedgerCore.service;

import com.maheer9272.LedgerCore.dto.DepositRequestDto;
import com.maheer9272.LedgerCore.dto.TransactionResponseDto;
import com.maheer9272.LedgerCore.dto.TransferRequestDto;
import com.maheer9272.LedgerCore.dto.WithdrawalRequestDto;
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

        DepositRequestDto requestDto = new DepositRequestDto(
                accountNumber,
                depositAmount,
                "Test description"
        );
        transactionService.deposit(
                requestDto,
                authentication,
                "Test-123"
        );

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

    @Test
    void shouldReturnDepositWhenIdempotencyKeyIsReused(){
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
                .thenReturn(oldHash);
        when(idempotencyRecordRepository.findByIdempotencyKey("Idem123"))
                .thenReturn(Optional.of(idempotencyRecord)
                );

        TransactionResponseDto responseDto =
                transactionService.deposit(
                        requestDto,
                        authentication,
                        "Idem123"
                );

        assertEquals(
                TransactionType.DEPOSIT,
                responseDto.getTransactionType()
        );

        assertEquals(
                depositAmount,
                responseDto.getAmount()
        );

        assertEquals(
                TransactionStatus.PENDING,
                responseDto.getStatus()
        );

        assertEquals(
                "Old deposit",
                responseDto.getMessage()
        );

        verify(transactionRepository, never())
                .save(any(FinancialTransaction.class));

        verify(ledgerEntryRepository, never())
                .save(any(LedgerEntry.class));
    }

    @Test
    void shouldWithdrawSuccessfully(){

        // Arrange
        String accountNumber = "12345678";
        BigDecimal withdrawAmount = new BigDecimal("1000.00");
        User destinationUser = new User(
                "Test User",
                "test@example.com",
                "password"
        );
        Account userAccount = new Account(destinationUser);
        userAccount.credit(new BigDecimal("10000.00"));
        Account systemAccount = Account
                .createSystemAccount(new BigDecimal("100000.00"));

        //Tell mockito what CurrentUserResolver should return basically Stubbing
        when(currentUserResolver.resolve(authentication))
                .thenReturn(destinationUser);

        when(idempotencyRecordService.getHash(
                accountNumber,
                withdrawAmount
        )).thenReturn("test-request-hash");

        when(idempotencyRecordRepository.findByIdempotencyKey("Test-123"))
                .thenReturn(Optional.empty());

        when(accountRepository.findByAccountNumberAndUserIdForUpdate(
                eq(accountNumber),
                nullable(UUID.class)))
                .thenReturn(Optional.of(userAccount));

        when(accountRepository.findByAccountTypeForUpdate(AccountType.SYSTEM))
                .thenReturn(Optional.of(systemAccount));

        when(transactionRepository.save(any(FinancialTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(ledgerEntryRepository.save(any(LedgerEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(ledgerEntryRepository.sumAmountByTransactionAndEntryType(
                nullable(UUID.class),
                eq(LedgerEntryType.DEBIT)
        )).thenReturn(withdrawAmount);

        when(ledgerEntryRepository.sumAmountByTransactionAndEntryType(
                nullable(UUID.class),
                eq(LedgerEntryType.CREDIT)
        )).thenReturn(withdrawAmount);

        WithdrawalRequestDto requestDto = new WithdrawalRequestDto(
                accountNumber,
                withdrawAmount,
                "Test description"
        );
        transactionService.withdraw(
                requestDto,
                authentication,
                "Test-123"
        );

        //Assert
        assertEquals(
                new BigDecimal("9000.00"),
                userAccount.getBalance()
        );
        assertEquals(
                new BigDecimal("101000.00"),
                systemAccount.getBalance()
        );
        verify(transactionRepository).save(any(FinancialTransaction.class));
        verify(ledgerEntryRepository, times(2))
                .save(any(LedgerEntry.class));
        verify(idempotencyRecordRepository)
                .save(any(IdempotencyRecord.class));
    }

    @Test
    void withdrawalRejectedForInsufficientBalance(){
        User sourceUser = new User(
                "Withdrawal test",
                "withdraw@example.com",
                "pass123"
        );
        Account userAccount = new Account(sourceUser);
        String accountNumber = "87654321";
        BigDecimal withdrawalAmount = new BigDecimal("25000");
        Account systemAccount =
                Account.createSystemAccount(new BigDecimal("100000.00"));

        when(currentUserResolver.resolve(authentication))
                .thenReturn(sourceUser);
        when(idempotencyRecordService.getHash(accountNumber,withdrawalAmount))
                .thenReturn("new-hash");
        when(idempotencyRecordRepository.findByIdempotencyKey("Test-123"))
                .thenReturn(Optional.empty());
        when(accountRepository.findByAccountTypeForUpdate(AccountType.SYSTEM))
                .thenReturn(Optional.of(systemAccount));
        when(accountRepository.findByAccountNumberAndUserIdForUpdate
                (eq(accountNumber),
                nullable(UUID.class)))
                .thenReturn(Optional.of(userAccount));

        when(transactionRepository.save(any(FinancialTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(ledgerEntryRepository.save(any(LedgerEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(ledgerEntryRepository.sumAmountByTransactionAndEntryType(
                nullable(UUID.class),
                eq(LedgerEntryType.DEBIT)
        )).thenReturn(withdrawalAmount);

        when(ledgerEntryRepository.sumAmountByTransactionAndEntryType(
                nullable(UUID.class),
                eq(LedgerEntryType.CREDIT)
        )).thenReturn(withdrawalAmount);

        WithdrawalRequestDto requestDto = new WithdrawalRequestDto(
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
    }

    @Test
    void shouldTransferSuccessfully(){
        String fromAccountNumber="12345678";
        String toAccountNumber="87654321";
        User sourceUser = new User(
                "Source User",
                "source@example.com",
                "pass123"
        );
        User destinationUser = new User(
                "destination User",
                "destination@example.com",
                "pass123"
        );
        Account sourceAccount = new Account(sourceUser);
        Account destinationAccount = new Account(destinationUser);
        sourceAccount.credit(new BigDecimal("5000.00"));
        destinationAccount.credit(new BigDecimal("5000.00"));
        BigDecimal transferAmount = new BigDecimal("1000.00");

        when(currentUserResolver.resolve(authentication))
                .thenReturn(sourceUser);

        when(idempotencyRecordService.getHash(fromAccountNumber,toAccountNumber,transferAmount))
                .thenReturn("new-hash");

        when(idempotencyRecordRepository.findByIdempotencyKey("new-hash"))
                .thenReturn(Optional.empty());

        when(accountRepository.findByAccountNumberAndUserIdForUpdate(
                eq(fromAccountNumber),
                nullable(UUID.class)))
                .thenReturn(Optional.of(sourceAccount));

        when(accountRepository.findByAccountNumberForUpdate(
                eq(toAccountNumber)))
                .thenReturn(Optional.of(destinationAccount));

        when(transactionRepository.save(any(FinancialTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(ledgerEntryRepository.save(any(LedgerEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        when(ledgerEntryRepository.sumAmountByTransactionAndEntryType(
                nullable(UUID.class),
                eq(LedgerEntryType.DEBIT)
        )).thenReturn(transferAmount);

        when(ledgerEntryRepository.sumAmountByTransactionAndEntryType(
                nullable(UUID.class),
                eq(LedgerEntryType.CREDIT)
        )).thenReturn(transferAmount);

        TransferRequestDto requestDto = new TransferRequestDto(
                fromAccountNumber,
                toAccountNumber,
                transferAmount,
                "Transfer Test"
        );

        transactionService.transfer(
                requestDto,
                authentication,
                "new-hash"
        );

        //Assert
        assertEquals(
                new BigDecimal("4000.00"),
                sourceAccount.getBalance()
        );
        assertEquals(
                new BigDecimal("6000.00"),
                destinationAccount.getBalance()
        );
        verify(transactionRepository).save(any(FinancialTransaction.class));
        verify(ledgerEntryRepository, times(2))
                .save(any(LedgerEntry.class));
        verify(idempotencyRecordRepository)
                .save(any(IdempotencyRecord.class));
    }


    @Test
    void rejectSameAccountTransfer(){
        String fromAccountNumber = "12345678";
        String toAccountNumber = "12345678";
        BigDecimal transferAmount = new BigDecimal("1000.00");
        User sourceUser=new User(
          "test user",
          "test@gmail.com",
          "pass123"
        );
        when(currentUserResolver.resolve(authentication))
                .thenReturn(sourceUser);
        TransferRequestDto transferRequestDto = new TransferRequestDto(
                fromAccountNumber,
                toAccountNumber,
                transferAmount,
                "transfer test 3"
        );
        assertThrows(InvalidTransactionRequestException.class,
                ()->transactionService.transfer(
                        transferRequestDto,
                        authentication,
                        "Test-123"
                ));
    }

    @Test
    void transferWithInsufficientBalanceRejected(){
        //Arranges
        String fromAccountNumber="12345678";
        String toAccountNumber="87654321";

        User sourceUser = new User(
                "Source User",
                "source@example.com",
                "pass123"
        );
        User destinationUser = new User(
                "destination User",
                "destination@example.com",
                "pass123"
        );

        Account sourceAccount = new Account(sourceUser);
        Account destinationAccount = new Account(destinationUser);

        sourceAccount.credit(new BigDecimal("5000.00"));
        destinationAccount.credit(new BigDecimal("5000.00"));
        BigDecimal transferAmount = new BigDecimal("60000.00");

        when(currentUserResolver.resolve(authentication))
                .thenReturn(sourceUser);
        when(idempotencyRecordService.getHash(fromAccountNumber,toAccountNumber,transferAmount))
                .thenReturn("new-hash");
        when(idempotencyRecordRepository.findByIdempotencyKey("new-hash"))
                .thenReturn(Optional.empty());
        when(accountRepository.findByAccountNumberAndUserIdForUpdate(
                eq(fromAccountNumber),
                nullable(UUID.class)))
                .thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByAccountNumberForUpdate(
                eq(toAccountNumber)))
                .thenReturn(Optional.of(destinationAccount));
        when(transactionRepository.save(any(FinancialTransaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(ledgerEntryRepository.save(any(LedgerEntry.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(ledgerEntryRepository.sumAmountByTransactionAndEntryType(
                nullable(UUID.class),
                eq(LedgerEntryType.DEBIT)
        )).thenReturn(transferAmount);
        when(ledgerEntryRepository.sumAmountByTransactionAndEntryType(
                nullable(UUID.class),
                eq(LedgerEntryType.CREDIT)
        )).thenReturn(transferAmount);

        TransferRequestDto requestDto = new TransferRequestDto(
                fromAccountNumber,
                toAccountNumber,
                transferAmount,
                "Transfer Test"
        );

        //Act & Assert
        assertThrows(InsufficientBalanceException.class,
                ()->transactionService.transfer(
                        requestDto,
                        authentication,
                        "new-hash"
                ));
    }
}