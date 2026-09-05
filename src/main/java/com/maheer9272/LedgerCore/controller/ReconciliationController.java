package com.maheer9272.LedgerCore.controller;

import com.maheer9272.LedgerCore.dto.ReconciliationResponseDto;
import com.maheer9272.LedgerCore.service.ReconciliationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/reconciliation/accounts")
public class ReconciliationController {

    private final ReconciliationService reconciliationService;

    public ReconciliationController(ReconciliationService reconciliationService) {
        this.reconciliationService = reconciliationService;
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<ReconciliationResponseDto> balanceIndifference(
            Authentication authentication,
            @PathVariable String accountNumber){
        ReconciliationResponseDto responseDto =
                reconciliationService.isAccountBalanced(authentication, accountNumber);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }
}
