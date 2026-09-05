package com.maheer9272.LedgerCore.service;

import com.maheer9272.LedgerCore.entity.LedgerEntry;
import com.maheer9272.LedgerCore.entity.TransactionStatus;
import com.maheer9272.LedgerCore.entity.TransactionType;
import org.springframework.data.jpa.domain.Specification;

public class TransactionHistorySpecification {

    public static Specification<LedgerEntry> hasAccountNumber(
            String accountNumber) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("account").get("accountNumber"),
                        accountNumber
                );
    }

    public static Specification<LedgerEntry> hasTransactionType(
            TransactionType transactionType) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("financialTransaction")
                                .get("transactionType"),
                        transactionType
                );
    }

    public static Specification<LedgerEntry> hasTransactionStatus(
            TransactionStatus transactionStatus) {

        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(
                        root.get("financialTransaction")
                                .get("transactionStatus"),
                        transactionStatus
                );
    }
}