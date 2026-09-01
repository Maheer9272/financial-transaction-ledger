package com.maheer9272.LedgerCore.repository;

import com.maheer9272.LedgerCore.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {

    Account getAccountByAccountNumber(String accountNumber);

    //Find accounts by user ID which is present as FK in Accounts table
    List<Account> findByUserId(UUID userId);

    Optional<Account> findByAccountNumberAndUserId(
            String accountNumber, UUID userId);

}
