package com.maheer9272.LedgerCore.exception;

public class InvalidTransactionStateException extends IllegalStateException {
    public InvalidTransactionStateException(String message) {
        super(message);
    }
}
