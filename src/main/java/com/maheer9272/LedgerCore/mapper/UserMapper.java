package com.maheer9272.LedgerCore.mapper;

import com.maheer9272.LedgerCore.dto.AccountResponseDto;
import com.maheer9272.LedgerCore.dto.CreateUserRequestDto;
import com.maheer9272.LedgerCore.dto.CreateUserResponseDto;
import com.maheer9272.LedgerCore.dto.UserProfileResponse;
import com.maheer9272.LedgerCore.entity.Account;
import com.maheer9272.LedgerCore.entity.User;
import org.springframework.stereotype.Component;

import java.awt.color.ProfileDataException;

@Component
public class UserMapper {

    public User mapToUser(CreateUserRequestDto requestDto) {
        return new User(
                requestDto.getName(),
                requestDto.getEmail(),
                requestDto.getPassword()
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

    public UserProfileResponse mapProfileToResponse(Account account){
        return new UserProfileResponse(
                account.getUser().getName(),
                account.getUser().getEmail(),
                account.getBalance()
        );
    }

}
