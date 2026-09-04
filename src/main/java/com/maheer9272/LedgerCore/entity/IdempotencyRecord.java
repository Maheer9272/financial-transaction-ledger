package com.maheer9272.LedgerCore.entity;

import jakarta.persistence.*;
import lombok.Getter;
import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "idempotency_keys")
public class IdempotencyRecord {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(
            name = "idempotency_key",
            nullable = false,
            unique = true,
            updatable = false
    )
    private String idempotencyKey;

    @OneToOne
    @JoinColumn(
            name = "transaction_id",
            unique = true,
            updatable = false,
            nullable = false)
    private FinancialTransaction financialTransaction;

    @Column(
            updatable = false,
            nullable = false,
            length = 64
    )
    private String requestHash;

    @Column(
            updatable = false,
            nullable = false
    )
    private Instant createdAt;

    protected IdempotencyRecord() {
    }

    public IdempotencyRecord(String idempotencyKey, FinancialTransaction financialTransaction,String requestHash) {
        this.idempotencyKey = idempotencyKey;
        this.financialTransaction = financialTransaction;
        this.requestHash=requestHash;
        this.createdAt = Instant.now();
    }
}
