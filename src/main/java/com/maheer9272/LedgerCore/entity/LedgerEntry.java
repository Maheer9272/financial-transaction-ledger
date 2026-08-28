package com.maheer9272.LedgerCore.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "ledger_entries")
public class LedgerEntry {

    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "financial_transaction_id",
            nullable = false,
            updatable = false
    )
    private FinancialTransaction financialTransaction;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "account_id",
            nullable = false,
            updatable = false
    )
    private Account account;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            updatable = false
    )
    private LedgerEntryType entryType;

    @NotNull
    @Positive
    @Column(
            precision = 19,
            scale = 4,
            nullable = false
    )
    private BigDecimal amount;

    @Column(
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    protected LedgerEntry() {
    }

    public LedgerEntry(
            FinancialTransaction financialTransaction,
            Account account,
            LedgerEntryType entryType,
            BigDecimal amount
    ) {
        this.financialTransaction = financialTransaction;
        this.account = account;
        this.entryType = entryType;
        this.amount = amount;
        this.createdAt = Instant.now();
    }
}