package com.maheer9272.LedgerCore.mapper;

import com.maheer9272.LedgerCore.dto.AccountResponseDto;
import com.maheer9272.LedgerCore.dto.CreateUserRequestDto;
import com.maheer9272.LedgerCore.dto.CreateUserResponseDto;
import com.maheer9272.LedgerCore.dto.UserProfileResponse;
import com.maheer9272.LedgerCore.entity.Account;
import com.maheer9272.LedgerCore.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public User mapToUser(CreateUserRequestDto requestDto, String encodedPassword) {
        return new User(
                requestDto.getName(),
                requestDto.getEmail(),
                encodedPassword
        );
    }

    public CreateUserResponseDto mapToResponse(User user, Account account) {

        AccountResponseDto accountResponse = new AccountResponseDto(
                account.getAccountNumber(),
                account.getBalance()
        );

        return new CreateUserResponseDto(
                user.getName(),
                user.getEmail(),
                "User created successfully",
                accountResponse
        );
    }

    public UserProfileResponse mapProfileToResponse(User user, Account account) {
        return new UserProfileResponse(
                user.getName(),
                user.getEmail(),
                account.getBalance()
        );
    }

}
