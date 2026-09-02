package com.maheer9272.LedgerCore.repository;

import com.maheer9272.LedgerCore.entity.FinancialTransaction;
import com.maheer9272.LedgerCore.entity.LedgerEntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.UUID;

public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, UUID> {

}
