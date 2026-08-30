package com.maheer9272.LedgerCore.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue
    private UUID id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column(
            nullable = false,
            length = 50
    )
    private String name;

    @Email
    @NotBlank
    @Size(max = 50)
    @Column(
            nullable = false,
            unique = true,
            length = 50
    )
    private String email;

    @NotBlank
    @Column(
            nullable = false,
            length = 100
    )
    private String password;

    @Column(
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Enumerated(EnumType.STRING)
    @Column(
            nullable = false,
            length = 20
    )
    private UserStatus userStatus;

    protected User() {
    }

    public User(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password=password;
        this.userStatus = UserStatus.ACTIVE;

        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void setName(String name) {
        this.name = name;
        this.updatedAt = Instant.now();
    }

    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = Instant.now();
    }

    /* These activate, suspend, close are written because if there is a setter setAccountStatus
    that's a very big problems cause anybody could have access to this easily can set the
    status to anything that's a problem here so we use methods to actually change the
    status properly
    */
    public void activate() {
        if (this.userStatus == UserStatus.CLOSED) {
            throw new IllegalStateException(
                    "Inactive user cannot be activated"
            );
        }

        this.userStatus = UserStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void suspend() {
        if (this.userStatus == UserStatus.CLOSED) {
            throw new IllegalStateException(
                    "Inactive user cannot be suspended"
            );
        }

        this.userStatus = UserStatus.SUSPENDED;
        this.updatedAt = Instant.now();
    }

    public void close() {
        if (this.userStatus == UserStatus.CLOSED) {
            throw new IllegalStateException(
                    "User is already inactive"
            );
        }

        this.userStatus = UserStatus.CLOSED;
        this.updatedAt = Instant.now();
    }
}