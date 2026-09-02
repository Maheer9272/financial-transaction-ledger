package com.maheer9272.LedgerCore.controller;

import com.maheer9272.LedgerCore.dto.*;
import com.maheer9272.LedgerCore.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponseDto> deposit(
            @Valid @RequestBody DepositRequestDto requestDto,
            Authentication authentication) {
        TransactionResponseDto responseDto =
                transactionService.deposit(requestDto, authentication);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponseDto> withdraw(
            @Valid @RequestBody WithdrawalRequestDto requestDto,
            Authentication authentication) {
        TransactionResponseDto responseDto =
                transactionService.withdraw(requestDto, authentication);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransferResponseDto> transfer(
            @Valid @RequestBody TransferRequestDto requestDto,
            Authentication authentication) {
        TransferResponseDto responseDto =
                transactionService.transfer(requestDto, authentication);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }

}
