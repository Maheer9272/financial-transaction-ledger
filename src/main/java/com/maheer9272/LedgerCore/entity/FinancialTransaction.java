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
@Table(name = "financial_transactions")
public class FinancialTransaction {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(
            unique = true,
            nullable = false,
            length = 50,
            insertable = false,
            updatable = false
    )
    private String referenceId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus transactionStatus;

    @NotNull
    @Positive
    @Column(
            precision = 19,
            scale = 4,
            nullable = false
    )
    private BigDecimal amount;

    private String description;

    @Column(
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(updatable = false)
    private Instant completedAt;

    protected FinancialTransaction() {
    }

    public FinancialTransaction(
            TransactionType transactionType,
            BigDecimal amount,
            String description
    ) {
        this.transactionType = transactionType;
        this.transactionStatus = TransactionStatus.PENDING;
        this.amount = amount;
        this.description = description;
        this.createdAt = Instant.now();
    }
}