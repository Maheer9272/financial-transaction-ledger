package com.maheer9272.LedgerCore.repository;

import com.maheer9272.LedgerCore.entity.LedgerEntry;
import com.maheer9272.LedgerCore.entity.LedgerEntryType;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.UUID;

public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, UUID>, JpaSpecificationExecutor<LedgerEntry> {
    @Query("""
                SELECT COALESCE(SUM(l.amount), 0)
                FROM LedgerEntry l
                WHERE l.financialTransaction.id = :transactionId
                  AND l.entryType = :entryType
            """)
    BigDecimal sumAmountByTransactionAndEntryType(
            UUID transactionId,
            LedgerEntryType entryType
    );

    @Query("""
                SELECT COALESCE(SUM(l.amount), 0)
                FROM LedgerEntry l
                WHERE l.account.id = :accountId
                  AND l.entryType = :entryType
            """)
    BigDecimal sumAmountByAccountIdAndEntryType(
            UUID accountId,
            LedgerEntryType entryType
    );
}
