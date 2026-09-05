package com.maheer9272.LedgerCore.controller;

import com.maheer9272.LedgerCore.dto.AccountResponseDto;
import com.maheer9272.LedgerCore.dto.TransactionHistoryResponseDto;
import com.maheer9272.LedgerCore.entity.TransactionStatus;
import com.maheer9272.LedgerCore.entity.TransactionType;
import com.maheer9272.LedgerCore.service.AccountService;
import com.maheer9272.LedgerCore.service.TransactionService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/accounts")
public class AccountController {
    private final AccountService accountService;
    private final TransactionService transactionService;

    public AccountController(AccountService accountService, TransactionService transactionService) {
        this.accountService = accountService;
        this.transactionService = transactionService;
    }

    @GetMapping
    public ResponseEntity<List<AccountResponseDto>> getAllAccounts(
            Authentication authentication
    ) {
        List<AccountResponseDto> responseDto =
                accountService.getAllAccountsByEmail(authentication);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<AccountResponseDto> getAccountByAccountNumber(
            @PathVariable String accountNumber, Authentication authentication
    ) {
        AccountResponseDto responseDto =
                accountService.getAccountByAccountNumber(accountNumber, authentication);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }

    @PostMapping
    public ResponseEntity<AccountResponseDto> createAccount(Authentication authentication) {

        AccountResponseDto responseDto =
                accountService.createAdditionalAccount(authentication);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseDto);
    }

    @GetMapping("/{accountNumber}/transactions")
    public ResponseEntity<Page<TransactionHistoryResponseDto>> getTransactionHistory(
            Authentication authentication,
            @PathVariable String accountNumber,
            @RequestParam(required = false) TransactionType transactionType,
            @RequestParam(required = false) TransactionStatus transactionStatus,
            Pageable pageable) {

        Page<TransactionHistoryResponseDto> responseDto =
                transactionService.getTransactionHistory(
                        authentication, accountNumber, transactionType, transactionStatus, pageable);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }
}
