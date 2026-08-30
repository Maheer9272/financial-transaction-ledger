package com.maheer9272.LedgerCore.controller;

import com.maheer9272.LedgerCore.dto.*;
import com.maheer9272.LedgerCore.repository.AccountRepository;
import com.maheer9272.LedgerCore.service.AccountService;
import com.maheer9272.LedgerCore.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
    @RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final AccountService accountService;

    public UserController(UserService userService, AccountService accountService, AccountRepository accountRepository) {
        this.userService = userService;
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<CreateUserResponseDto> createUser(
            @Valid @RequestBody CreateUserRequestDto requestDto){

        CreateUserResponseDto responseDto = userService.createUser(requestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
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
