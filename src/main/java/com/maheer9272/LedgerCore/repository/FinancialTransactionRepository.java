package com.maheer9272.LedgerCore.repository;

import com.maheer9272.LedgerCore.entity.FinancialTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, UUID> {
}
