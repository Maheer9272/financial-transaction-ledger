package com.maheer9272.LedgerCore.service;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Service
public class IdempotencyRecordService {

    public String getHash(String accountNumber, BigDecimal amount) {
        String data = accountNumber + "-" + amount.toPlainString();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }

    }

    public String getHash( String fromAccountNumber,
                           String toAccountNumber,
                           BigDecimal amount) {
        String data = fromAccountNumber + "-" + toAccountNumber+ "-" + amount.toPlainString();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }

    }
}
