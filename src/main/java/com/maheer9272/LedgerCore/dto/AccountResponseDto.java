package com.maheer9272.LedgerCore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class AccountResponseDto {

    private String accountNumber;
    private BigDecimal balance;
}