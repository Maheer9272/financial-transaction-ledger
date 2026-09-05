package com.maheer9272.LedgerCore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReconciliationResponseDto {
    private String accountNumber;
    private BigDecimal accountBalance;
    private BigDecimal ledgerBalance;
    private BigDecimal difference;
    private Boolean reconciled;
}
