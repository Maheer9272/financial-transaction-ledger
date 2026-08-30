package com.maheer9272.LedgerCore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserUpdateRequestDto {

    @Size(
            min = 3,
            max = 50,
            message = "name must be between 3-50 characters"
    )
    private String name;

    @Email(message = "Email must be valid")
    @Size(
            max = 50,
            message = "Email must be less than 50 characters"
    )
    private String email;

}
