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
public class CreateUserRequestDto {

    @NotBlank(message = "Name is required")
    @Size(
            min = 3,
            max = 50,
            message = "name must be between 3-50 characters"
    )
    private String name;

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
