package com.maheer9272.LedgerCore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequestDto {
    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(
            max = 50,
            message = "Email must be less than 50 characters"
    )
    private String email;
}
