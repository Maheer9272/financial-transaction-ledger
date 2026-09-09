package com.maheer9272.LedgerCore.exception;

public class InvalidTransactionRequestException extends IllegalArgumentException {
    public InvalidTransactionRequestException(String message) {
        super(message);
    }
}
