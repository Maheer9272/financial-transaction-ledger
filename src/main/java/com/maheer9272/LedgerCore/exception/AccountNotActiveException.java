package com.maheer9272.LedgerCore.exception;

public class AccountNotActiveException extends IllegalStateException {
    public AccountNotActiveException(String message) {
        super(message);
    }
}
