package com.maheer9272.LedgerCore.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "accounts")
public class Account {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(
            nullable = false,
            unique = true,
            length = 8,
            updatable = false,
            insertable = false
    )
    private String accountNumber;

    @NotNull
    @PositiveOrZero
    @Column(
            precision = 19,
            scale = 4,
            nullable = false
    )
    private BigDecimal balance;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatus accountStatus;

    @Version
    @NotNull
    @Column(nullable = false)
    private Long version;

    @Column(
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType accountType;

    protected Account() {
    }

    // Customer account
    public Account(User user) {
        this.user = user;
        this.accountType = AccountType.CUSTOMER;
        this.balance = BigDecimal.ZERO;
        this.accountStatus = AccountStatus.ACTIVE;

        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    // System account
    public static Account createSystemAccount() {
        return new Account(AccountType.SYSTEM);
    }

    private Account(AccountType accountType) {
        this.accountType = accountType;
        this.balance = BigDecimal.ZERO;
        this.accountStatus = AccountStatus.ACTIVE;

        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }


    /* These activate, suspend, close are written because if there is a setter setAccountStatus
    that's a very big problems cause anybody could have access to this easily can set the
    status to anything that's a problem here so we use methods to actually change the
    status properly
    */

    public void activate() {
        if (this.accountStatus == AccountStatus.CLOSED) {
            throw new IllegalStateException(
                    "Closed account cannot be activated"
            );
        }

        this.accountStatus = AccountStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void suspend() {
        if (this.accountStatus == AccountStatus.CLOSED) {
            throw new IllegalStateException(
                    "Closed account cannot be suspended"
            );
        }

        this.accountStatus = AccountStatus.SUSPENDED;
        this.updatedAt = Instant.now();
    }

    public void close() {
        if (this.accountStatus == AccountStatus.CLOSED) {
            throw new IllegalStateException(
                    "Account is already closed"
            );
        }

        this.accountStatus = AccountStatus.CLOSED;
        this.updatedAt = Instant.now();
    }
}