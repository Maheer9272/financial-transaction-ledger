package com.maheer9272.LedgerCore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class UserProfileResponse {
    private String name;
    private String email;
    private BigDecimal balance;
}