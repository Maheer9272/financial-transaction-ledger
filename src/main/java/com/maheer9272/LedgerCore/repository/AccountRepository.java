package com.maheer9272.LedgerCore.repository;

import com.maheer9272.LedgerCore.entity.Account;
import com.maheer9272.LedgerCore.entity.AccountType;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    //Find accounts by user ID which is present as FK in Accounts table
    List<Account> findByUserId(UUID userId);

    Optional<Account> findByAccountNumberAndUserId(String accountNumber, UUID userId);

    Optional<Account> findByAccountType(AccountType accountType);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT a
            FROM Account a
            WHERE a.accountNumber = :accountNumber
            AND a.user.id = :userId
            """)
    Optional<Account> findByAccountNumberAndUserIdForUpdate(@Param("accountNumber") String accountNumber, @Param("userId") UUID userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT a
            FROM Account a
            WHERE a.accountType = :accountType
            """)
    Optional<Account> findByAccountTypeForUpdate(@Param("accountType") AccountType accountType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT a
            FROM Account a
            WHERE a.id = :id
            """)
    Optional<Account> findByIdForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT a
            FROM Account a
            WHERE a.accountNumber = :accountNumber
            """)
    Optional<Account> findByAccountNumberForUpdate(@Param("accountNumber") String accountNumber);
}
