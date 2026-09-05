package com.maheer9272.LedgerCore.mapper;

import com.maheer9272.LedgerCore.dto.TransactionHistoryResponseDto;
import com.maheer9272.LedgerCore.entity.FinancialTransaction;
import com.maheer9272.LedgerCore.entity.LedgerEntry;
import org.springframework.stereotype.Component;

@Component
public class TransactionMapper {

    public TransactionHistoryResponseDto mapToTransactionHistoryResponse(
            LedgerEntry ledgerEntry) {

        FinancialTransaction transaction =
                ledgerEntry.getFinancialTransaction();

        return new TransactionHistoryResponseDto(
                transaction.getReferenceId(),
                transaction.getTransactionType(),
                ledgerEntry.getAmount(),
                transaction.getTransactionStatus(),
                ledgerEntry.getEntryType(),
                transaction.getDescription(),
                transaction.getCreatedAt(),
                transaction.getCompletedAt()
        );
    }
}
