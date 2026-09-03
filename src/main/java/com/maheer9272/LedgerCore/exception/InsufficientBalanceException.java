package com.maheer9272.LedgerCore.exception;

public class InsufficientBalanceException extends IllegalStateException {
    public InsufficientBalanceException(String message) {
        super(message);
    }
}
