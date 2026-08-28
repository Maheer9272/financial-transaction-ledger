package com.maheer9272.LedgerCore.repository;

import com.maheer9272.LedgerCore.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
}
