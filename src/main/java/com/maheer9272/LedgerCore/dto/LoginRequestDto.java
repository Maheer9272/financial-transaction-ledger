package com.maheer9272.LedgerCore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Size(
            max = 50,
            message = "Email must be less than 50 characters"
    )
    private String email;

    @NotBlank(message = "Password required")
    @Size(
            min = 8,
            max = 25,
            message = "Password must be between 8-25 characters"
    )
    private String password;
}