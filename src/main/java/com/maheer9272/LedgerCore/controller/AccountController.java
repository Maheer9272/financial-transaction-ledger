package com.maheer9272.LedgerCore.controller;

import com.maheer9272.LedgerCore.dto.AccountResponseDto;
import com.maheer9272.LedgerCore.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/accounts")
public class AccountController {
    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
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

}
