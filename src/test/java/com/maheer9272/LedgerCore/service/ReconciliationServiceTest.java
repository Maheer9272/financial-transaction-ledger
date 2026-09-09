package com.maheer9272.LedgerCore.service;

import com.maheer9272.LedgerCore.dto.ReconciliationResponseDto;
import com.maheer9272.LedgerCore.entity.Account;
import com.maheer9272.LedgerCore.entity.LedgerEntryType;
import com.maheer9272.LedgerCore.entity.User;
import com.maheer9272.LedgerCore.repository.AccountRepository;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReconciliationServiceTest {
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private LedgerEntryRepository ledgerEntryRepository;
    @Mock
    private CurrentUserResolver currentUserResolver;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private ReconciliationService reconciliationService;

    @Test
    void successfulReconciliationOfBalancedAccount(){
        String accountNumber = "12345678";
        User user = new User(
          "Test user",
          "test@gmail.com",
          "test@123"
        );
        Account account = new Account(user);
        BigDecimal debitAmountTotal = new BigDecimal("1000.00");
        BigDecimal creditAmountTotal = new BigDecimal("1000.00");

        when(currentUserResolver.resolve(authentication))
                .thenReturn(user);
        when(accountRepository.findByAccountNumber(eq(accountNumber)))
                .thenReturn(Optional.of(account));
        when(ledgerEntryRepository.sumAmountByAccountIdAndEntryType(
                nullable(UUID.class),
                eq(LedgerEntryType.DEBIT)
        )).thenReturn(debitAmountTotal);
        when(ledgerEntryRepository.sumAmountByAccountIdAndEntryType(
                nullable(UUID.class),
                eq(LedgerEntryType.CREDIT)
        )).thenReturn(creditAmountTotal);

        ReconciliationResponseDto responseDto =reconciliationService
                .isAccountBalanced(authentication,accountNumber);

        assertEquals(
                new BigDecimal("0.00"),
                responseDto.getLedgerBalance()
        );
        assertEquals(
                new BigDecimal("0.00"),
                responseDto.getDifference()
        );

        assertTrue(responseDto.getReconciled());
    }

    @Test
    void accountBalanceMismatched(){
        String accountNumber = "12345678";
        User user = new User(
                "Test user",
                "test@gmail.com",
                "test@123"
        );
        Account account = new Account(user);
        BigDecimal debitAmount = new BigDecimal("1000.00");
        BigDecimal creditAmount = new BigDecimal("1010.00");

        when(currentUserResolver.resolve(authentication))
                .thenReturn(user);
        when(accountRepository.findByAccountNumber(eq(accountNumber)))
                .thenReturn(Optional.of(account));
        when(ledgerEntryRepository.sumAmountByAccountIdAndEntryType(
                nullable(UUID.class),
                eq(LedgerEntryType.DEBIT)
        )).thenReturn(debitAmount);
        when(ledgerEntryRepository.sumAmountByAccountIdAndEntryType(
                nullable(UUID.class),
                eq(LedgerEntryType.CREDIT)
        )).thenReturn(creditAmount);

        ReconciliationResponseDto responseDto =reconciliationService
                .isAccountBalanced(authentication,accountNumber);

        assertEquals(
                new BigDecimal("10.00"),
                responseDto.getLedgerBalance()
        );
        assertEquals(
                new BigDecimal("-10.00"),
                responseDto.getDifference()
        );

        assertFalse(responseDto.getReconciled());
    }

    @Test
    void accountWithNoLedgerEntries(){
        String accountNumber = "12345678";
        User user = new User(
                "Test user",
                "test@gmail.com",
                "test@123"
        );
        Account account = new Account(user);
        BigDecimal debitAmount = new BigDecimal("0.00");
        BigDecimal creditAmount = new BigDecimal("0.00");

        when(currentUserResolver.resolve(authentication))
                .thenReturn(user);
        when(accountRepository.findByAccountNumber(eq(accountNumber)))
                .thenReturn(Optional.of(account));
        when(ledgerEntryRepository.sumAmountByAccountIdAndEntryType(
                nullable(UUID.class),
                eq(LedgerEntryType.DEBIT)
        )).thenReturn(debitAmount);
        when(ledgerEntryRepository.sumAmountByAccountIdAndEntryType(
                nullable(UUID.class),
                eq(LedgerEntryType.CREDIT)
        )).thenReturn(creditAmount);

        ReconciliationResponseDto responseDto =reconciliationService
                .isAccountBalanced(authentication,accountNumber);

        assertEquals(
                new BigDecimal("0.00"),
                responseDto.getLedgerBalance()
        );
        assertEquals(
                new BigDecimal("0.00"),
                responseDto.getDifference()
        );

        assertTrue(responseDto.getReconciled());
    }
}


















