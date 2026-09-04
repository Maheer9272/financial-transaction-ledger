package com.maheer9272.LedgerCore.service;

import com.maheer9272.LedgerCore.entity.IdempotencyRecord;
import com.maheer9272.LedgerCore.repository.IdempotencyRecordRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Optional;

@Service
public class IdempotencyRecordService {
    private final IdempotencyRecordRepository idempotencyRecordRepository;

    public IdempotencyRecordService(IdempotencyRecordRepository idempotencyRecordRepository) {
        this.idempotencyRecordRepository = idempotencyRecordRepository;
    }

    public String getHash(String accountNumber, BigDecimal depositAmount) {
        String data = accountNumber + "-" + depositAmount.toPlainString();

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
                           BigDecimal transferAmount) {
        String data = fromAccountNumber + "-" + toAccountNumber+ "-" + transferAmount.toPlainString();

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm not available", e);
        }

    }
}
