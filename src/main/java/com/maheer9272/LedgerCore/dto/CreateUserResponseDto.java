package com.maheer9272.LedgerCore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserResponseDto {
    private String name;
    private String email;
    private String message;
    private AccountResponseDto accountResponseDto;
}
