package com.maheer9272.LedgerCore.service;

import com.maheer9272.LedgerCore.dto.*;
import com.maheer9272.LedgerCore.entity.*;
import com.maheer9272.LedgerCore.exception.*;
import com.maheer9272.LedgerCore.repository.AccountRepository;
import com.maheer9272.LedgerCore.repository.FinancialTransactionRepository;
import com.maheer9272.LedgerCore.repository.LedgerEntryRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class TransactionService {
    private final FinancialTransactionRepository transactionRepository;
    private final CurrentUserResolver currentUserResolver;
    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;

    public TransactionService(FinancialTransactionRepository transactionRepository, CurrentUserResolver currentUserResolver, AccountRepository accountRepository, LedgerEntryRepository ledgerEntryRepository) {
        this.transactionRepository = transactionRepository;
        this.currentUserResolver = currentUserResolver;
        this.accountRepository = accountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
    }

    @Transactional
    public TransactionResponseDto deposit(DepositRequestDto requestDto, Authentication authentication) {

        User sourceUser = currentUserResolver.resolve(authentication);
        if (sourceUser.getUserStatus() != UserStatus.ACTIVE) {
            throw new UserNotActiveException(
                    "User is not active"
            );
        }

        Account systemAccount = accountRepository
                        .findByAccountTypeForUpdate(AccountType.SYSTEM)
                        .orElseThrow(() ->
                                new SystemAccountNotFoundException(
                                        "SYSTEM account not found"
                                ));

        Account userAccount = accountRepository
                .findByAccountNumberAndUserIdForUpdate(
                        requestDto.getAccountNumber(),
                        sourceUser.getId())
                .orElseThrow(() ->
                        new ResourceDeniedException("This userAccount doesn't belong to you")
                );
        if (userAccount.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(
                    "Account is not active"
            );
        }

        BigDecimal depositAmount = requestDto.getAmount();

        //Creating a financial transaction
        String description = requestDto.getDescription();
        FinancialTransaction depositTransaction =
                new FinancialTransaction(
                        TransactionType.DEPOSIT,
                        depositAmount,
                        description
                );
        transactionRepository.save(depositTransaction);
        // Creating ledger entries
        LedgerEntry debitLedgerEntry = new LedgerEntry(
                depositTransaction,
                systemAccount,
                LedgerEntryType.DEBIT,
                depositAmount
        );

        LedgerEntry creditLedgerEntry = new LedgerEntry(
                depositTransaction,
                userAccount,
                LedgerEntryType.CREDIT,
                depositAmount
        );
        ledgerEntryRepository.save(debitLedgerEntry);
        ledgerEntryRepository.save(creditLedgerEntry);
        ledgerEntryRepository.flush();

        BigDecimal debitTotal =
                ledgerEntryRepository.sumAmountByTransactionAndEntryType(
                        depositTransaction.getId(),
                        LedgerEntryType.DEBIT
                );

        BigDecimal creditTotal =
                ledgerEntryRepository.sumAmountByTransactionAndEntryType(
                        depositTransaction.getId(),
                        LedgerEntryType.CREDIT
                );

        if (debitTotal.compareTo(creditTotal) != 0) {
            throw new TransactionNotBalancedException(
                    "Transaction ledger is not balanced"
            );
        }

        // Transferring the actual money
        systemAccount.debit(depositAmount);
        userAccount.credit(depositAmount);
        depositTransaction.complete();

        return new TransactionResponseDto(
                depositTransaction.getReferenceId(),
                depositTransaction.getTransactionType(),
                depositAmount,
                depositTransaction.getTransactionStatus(),
                "Deposited the amount of " + depositAmount + " successfully"
        );
    }

    @Transactional
    public TransactionResponseDto withdraw(WithdrawalRequestDto requestDto,
                                           Authentication authentication) {

        User user = currentUserResolver.resolve(authentication);
        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new UserNotActiveException(
                    "User is not active"
            );
        }
        Account systemAccount =
                accountRepository
                        .findByAccountTypeForUpdate(AccountType.SYSTEM)
                        .orElseThrow(() ->
                                new SystemAccountNotFoundException(
                                        "SYSTEM account not found"
                                ));

        Account userAccount = accountRepository
                .findByAccountNumberAndUserIdForUpdate(
                        requestDto.getAccountNumber(),
                        user.getId())
                .orElseThrow(() ->
                        new ResourceDeniedException("This userAccount doesn't belong to you")
                );
        if (userAccount.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(
                    "Account is not active"
            );
        }

        BigDecimal withdrawalAmount = requestDto.getAmount();

        //Creating a financial transaction
        String description = requestDto.getDescription();

        FinancialTransaction withdrawalTransaction =
                new FinancialTransaction(
                        TransactionType.WITHDRAWAL,
                        withdrawalAmount,
                        description
                );

        transactionRepository.save(withdrawalTransaction);
        // Creating ledger entries
        LedgerEntry debitLedgerEntry = new LedgerEntry(
                withdrawalTransaction,
                userAccount,
                LedgerEntryType.DEBIT,
                withdrawalAmount
        );

        LedgerEntry creditLedgerEntry = new LedgerEntry(
                withdrawalTransaction,
                systemAccount,
                LedgerEntryType.CREDIT,
                withdrawalAmount
        );
        ledgerEntryRepository.save(debitLedgerEntry);
        ledgerEntryRepository.save(creditLedgerEntry);
        ledgerEntryRepository.flush();

        BigDecimal debitTotal =
                ledgerEntryRepository.sumAmountByTransactionAndEntryType(
                        withdrawalTransaction.getId(),
                        LedgerEntryType.DEBIT
                );

        BigDecimal creditTotal =
                ledgerEntryRepository.sumAmountByTransactionAndEntryType(
                        withdrawalTransaction.getId(),
                        LedgerEntryType.CREDIT
                );

        if (debitTotal.compareTo(creditTotal) != 0) {
            throw new TransactionNotBalancedException(
                    "Transaction ledger is not balanced"
            );
        }

        // Transferring the actual money
        userAccount.debit(withdrawalAmount);
        systemAccount.credit(withdrawalAmount);
        withdrawalTransaction.complete();

        return new TransactionResponseDto(
                withdrawalTransaction.getReferenceId(),
                withdrawalTransaction.getTransactionType(),
                withdrawalAmount,
                withdrawalTransaction.getTransactionStatus(),
                "Withdrawn the amount of " + withdrawalAmount + " successfully"
        );
    }

    @Transactional
    public TransferResponseDto transfer(TransferRequestDto requestDto,
                                        Authentication authentication) {

        User sourceUser = currentUserResolver.resolve(authentication);
        if (sourceUser.getUserStatus() != UserStatus.ACTIVE) {
            throw new UserNotActiveException(
                    "User is not active"
            );
        }

        if (requestDto.getFromAccountNumber().equals(requestDto.getToAccountNumber())) {
            throw new IllegalArgumentException(
                    "Cannot transfer to the same account"
            );
        }

        Account sourceAccount;
        Account destinationAccount;

        /*
         Now checking the comparing the account number
         Lock the account first whichever account number is smaller
         */
        if (requestDto.getFromAccountNumber().compareTo(requestDto.getToAccountNumber()) < 0) {

            sourceAccount = accountRepository
                    .findByAccountNumberAndUserIdForUpdate(
                            requestDto.getFromAccountNumber(),
                            sourceUser.getId()
                    )
                    .orElseThrow(() ->
                            new ResourceDeniedException(
                                    "This account doesn't belong to you"
                            )
                    );

            destinationAccount = accountRepository
                    .findByAccountNumberForUpdate(requestDto.getToAccountNumber())
                    .orElseThrow(() ->
                            new ResourceDeniedException(
                                    "This account does not exist"
                            ));

        } else {

            destinationAccount = accountRepository
                    .findByAccountNumberForUpdate(requestDto.getToAccountNumber())
                    .orElseThrow(() ->
                            new ResourceDeniedException(
                                    "This account does not exist"
                            ));

            sourceAccount = accountRepository
                    .findByAccountNumberAndUserIdForUpdate(
                            requestDto.getFromAccountNumber(),
                            sourceUser.getId()
                    )
                    .orElseThrow(() ->
                            new ResourceDeniedException(
                                    "This account doesn't belong to you"
                            ));
        }

        if (sourceAccount.getAccountType() != AccountType.CUSTOMER) {
            throw new IllegalArgumentException(
                    "Transfers can only be made from customer accounts"
            );
        }
        if (sourceAccount.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(
                    "Account is not active"
            );
        }

        if (destinationAccount.getAccountType() != AccountType.CUSTOMER) {
            throw new IllegalArgumentException(
                    "Transfers can only be made to customer accounts"
            );
        }

        if (destinationAccount.getAccountStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(
                    "Account is not active"
            );
        }

        BigDecimal transferAmount = requestDto.getAmount();

        FinancialTransaction transferTransaction =
                new FinancialTransaction(
                        TransactionType.TRANSFER,
                        transferAmount,
                        requestDto.getDescription()
                );

        transactionRepository.save(transferTransaction);

        LedgerEntry debitLedgerEntry = new LedgerEntry(
                transferTransaction,
                sourceAccount,
                LedgerEntryType.DEBIT,
                transferAmount
        );

        LedgerEntry creditLedgerEntry = new LedgerEntry(
                transferTransaction,
                destinationAccount,
                LedgerEntryType.CREDIT,
                transferAmount
        );

        ledgerEntryRepository.save(debitLedgerEntry);
        ledgerEntryRepository.save(creditLedgerEntry);
        ledgerEntryRepository.flush();

        BigDecimal debitTotal =
                ledgerEntryRepository.sumAmountByTransactionAndEntryType(
                        transferTransaction.getId(),
                        LedgerEntryType.DEBIT
                );

        BigDecimal creditTotal =
                ledgerEntryRepository.sumAmountByTransactionAndEntryType(
                        transferTransaction.getId(),
                        LedgerEntryType.CREDIT
                );

        if (debitTotal.compareTo(creditTotal) != 0) {
            throw new TransactionNotBalancedException(
                    "Transaction ledger is not balanced"
            );
        }

        sourceAccount.debit(transferAmount);
        destinationAccount.credit(transferAmount);

        transferTransaction.complete();

        return new TransferResponseDto(
                transferTransaction.getReferenceId(),
                transferTransaction.getTransactionType(),
                transferAmount,
                transferTransaction.getTransactionStatus(),
                "Transferred the amount of " + transferAmount + " successfully"
        );
    }
}