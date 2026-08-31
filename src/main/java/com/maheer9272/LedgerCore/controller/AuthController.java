package com.maheer9272.LedgerCore.controller;

import com.maheer9272.LedgerCore.dto.CreateUserRequestDto;
import com.maheer9272.LedgerCore.dto.CreateUserResponseDto;
import com.maheer9272.LedgerCore.dto.LoginRequestDto;
import com.maheer9272.LedgerCore.dto.LoginResponseDto;
import com.maheer9272.LedgerCore.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<CreateUserResponseDto> registerUser(
            @Valid @RequestBody CreateUserRequestDto requestDto) {

        CreateUserResponseDto responseDto = authService.register(requestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseDto);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto loginRequestDto) {

        LoginResponseDto responseDto = authService.login(loginRequestDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }
}
