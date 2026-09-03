package com.maheer9272.LedgerCore.exception;

public class UserNotActiveException extends IllegalStateException {
    public UserNotActiveException(String message) {
        super(message);
    }
}
