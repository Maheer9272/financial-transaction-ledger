package com.maheer9272.LedgerCore.controller;

import com.maheer9272.LedgerCore.dto.UserProfileResponse;
import com.maheer9272.LedgerCore.dto.UserUpdateRequestDto;
import com.maheer9272.LedgerCore.dto.UserUpdateResponseDto;
import com.maheer9272.LedgerCore.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me/{accountNumber}")
    public ResponseEntity<UserProfileResponse> profile(
            @PathVariable String accountNumber,
            Authentication authentication) {
        UserProfileResponse response = userService.getProfile(accountNumber, authentication);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(response);
    }

    @PatchMapping("/me")
    public ResponseEntity<UserUpdateResponseDto> updateUser(
            @Valid @RequestBody UserUpdateRequestDto userUpdateRequestDto,
            Authentication authentication) {

        UserUpdateResponseDto responseDto = userService.updateUser(userUpdateRequestDto, authentication);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(responseDto);
    }

//    @GetMapping("/me/accounts")
//    public ResponseEntity<List<UserProfileResponse>> profile(@PathVariable String accountNumber){
//        UserProfileResponse response = userService.getProfile(accountNumber);
//        return ResponseEntity
//                .status(HttpStatus.OK)
//                .body(response);
//    }

}
