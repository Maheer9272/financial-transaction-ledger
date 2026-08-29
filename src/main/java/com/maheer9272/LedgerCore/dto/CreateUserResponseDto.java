package com.maheer9272.LedgerCore.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateUserResponseDto {
    private String name;
    private String email;
    private String message;
}
