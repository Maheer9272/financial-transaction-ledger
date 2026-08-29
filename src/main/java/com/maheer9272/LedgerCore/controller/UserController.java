package com.maheer9272.LedgerCore.controller;

import com.maheer9272.LedgerCore.dto.CreateUserRequestDto;
import com.maheer9272.LedgerCore.dto.CreateUserResponseDto;
import com.maheer9272.LedgerCore.entity.User;
import com.maheer9272.LedgerCore.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.classfile.instruction.ReturnInstruction;

@RestController
    @RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<CreateUserResponseDto> createUser(
            @Valid @RequestBody CreateUserRequestDto requestDto){

        CreateUserResponseDto responseDto = userService.createUser(requestDto);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseDto);
    }

}
