package com.maheer9272.LedgerCore.dto;

import com.maheer9272.LedgerCore.entity.LedgerEntryType;
import com.maheer9272.LedgerCore.entity.TransactionStatus;
import com.maheer9272.LedgerCore.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionHistoryResponseDto {
    private String referenceId;
    private TransactionType transactionType;
    private BigDecimal amount;
    private TransactionStatus status;
    private LedgerEntryType ledgerEntryType;
    private String description;
    private Instant createdAt;
    private Instant completedAt;
}
