package com.maheer9272.LedgerCore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DepositRequestDto {

    @NotBlank
    @Size(max = 8, message = "Enter a valid account number")
    private String accountNumber;

    @NotNull
    @Positive
    private BigDecimal amount;

    @Size(max = 100)
    private String description;
}
