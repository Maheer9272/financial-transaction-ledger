package com.maheer9272.LedgerCore.exception;

import org.springframework.dao.DataIntegrityViolationException;

public class IdempotencyKeyConflictException extends DataIntegrityViolationException {
    public IdempotencyKeyConflictException(String message) {
        super(message);
    }
}
