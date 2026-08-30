package com.maheer9272.LedgerCore.controller;

import com.maheer9272.LedgerCore.dto.*;
import com.maheer9272.LedgerCore.service.AccountService;
import com.maheer9272.LedgerCore.service.JwtService;
import com.maheer9272.LedgerCore.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<CreateUserResponseDto> registerUser(
            @Valid @RequestBody CreateUserRequestDto requestDto){

        CreateUserResponseDto responseDto = userService.register(requestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseDto);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @Valid @RequestBody LoginRequestDto loginRequestDto) {

        LoginResponseDto responseDto=userService.login(loginRequestDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }

    @GetMapping("/me/{accountNumber}")
    public ResponseEntity<UserProfileResponse> profile(@PathVariable String accountNumber){
        UserProfileResponse response = userService.getProfile(accountNumber);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

//    @PatchMapping implement this end point when you integrated the security
//    public ResponseEntity<UserUpdateResponseDto> updateUser(
//            @Valid @RequestBody UserUpdateRequestDto userUpdateRequestDto){
//        UserUpdateResponseDto responseDto=userService.updateUser(userUpdateRequestDto);
//    }

//    @GetMapping("/me/accounts")
//    public ResponseEntity<List<UserProfileResponse>> profile(@PathVariable String accountNumber){
//        UserProfileResponse response = userService.getProfile(accountNumber);
//        return ResponseEntity
//                .status(HttpStatus.OK)
//                .body(response);
//    }

}
