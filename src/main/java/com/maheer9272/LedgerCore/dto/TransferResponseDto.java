package com.maheer9272.LedgerCore.dto;

import com.maheer9272.LedgerCore.entity.TransactionStatus;
import com.maheer9272.LedgerCore.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TransferResponseDto {
    private String referenceId;
    private TransactionType transactionType;
    private BigDecimal amount;
    private TransactionStatus status;
    private String message;
}
