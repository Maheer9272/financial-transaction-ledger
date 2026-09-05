package com.maheer9272.LedgerCore.service;

import com.maheer9272.LedgerCore.dto.ReconciliationResponseDto;
import com.maheer9272.LedgerCore.entity.Account;
import com.maheer9272.LedgerCore.entity.LedgerEntryType;
import com.maheer9272.LedgerCore.entity.User;
import com.maheer9272.LedgerCore.exception.ResourceNotFoundException;
import com.maheer9272.LedgerCore.repository.AccountRepository;
import com.maheer9272.LedgerCore.repository.LedgerEntryRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class ReconciliationService {
    private final AccountRepository accountRepository;
    private final LedgerEntryRepository ledgerEntryRepository;
    private final CurrentUserResolver currentUserResolver;

    public ReconciliationService(AccountRepository accountRepository, LedgerEntryRepository ledgerEntryRepository, CurrentUserResolver currentUserResolver) {
        this.accountRepository = accountRepository;
        this.ledgerEntryRepository = ledgerEntryRepository;
        this.currentUserResolver = currentUserResolver;
    }

    @Transactional(readOnly = true)
    public ReconciliationResponseDto isAccountBalanced(Authentication authentication, String accountNumber) {
        User user = currentUserResolver.resolve(authentication);
        Account account = accountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        BigDecimal debitAmountTotal = ledgerEntryRepository
                .sumAmountByAccountIdAndEntryType(account.getId(), LedgerEntryType.DEBIT);

        BigDecimal creditAmountTotal = ledgerEntryRepository
                .sumAmountByAccountIdAndEntryType(account.getId(), LedgerEntryType.CREDIT);

        BigDecimal ledgerBalance = creditAmountTotal.subtract(debitAmountTotal);
        BigDecimal accountBalance = account.getBalance();
        BigDecimal difference = accountBalance.subtract(ledgerBalance);
        boolean isReconciled = ledgerBalance.compareTo(accountBalance)==0;
        return new ReconciliationResponseDto(
                account.getAccountNumber(),
                account.getBalance(),
                ledgerBalance,
                difference,
                isReconciled
        );
    }
}
