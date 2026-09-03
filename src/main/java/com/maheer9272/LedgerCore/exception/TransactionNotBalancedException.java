package com.maheer9272.LedgerCore.exception;

import org.hibernate.TransactionException;

public class TransactionNotBalancedException extends TransactionException {
    public TransactionNotBalancedException(String message) {
        super(message);
    }
}
