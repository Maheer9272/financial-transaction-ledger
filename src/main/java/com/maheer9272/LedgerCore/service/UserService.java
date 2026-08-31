package com.maheer9272.LedgerCore.service;

import com.maheer9272.LedgerCore.dto.UserProfileResponse;
import com.maheer9272.LedgerCore.dto.UserUpdateRequestDto;
import com.maheer9272.LedgerCore.dto.UserUpdateResponseDto;
import com.maheer9272.LedgerCore.entity.Account;
import com.maheer9272.LedgerCore.entity.User;
import com.maheer9272.LedgerCore.mapper.UserMapper;
import com.maheer9272.LedgerCore.repository.AccountRepository;
import com.maheer9272.LedgerCore.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AccountRepository accountRepository;


    public UserService(UserRepository userRepository,
                       UserMapper userMapper,
                       AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.accountRepository = accountRepository;
    }

    @Transactional
    public UserProfileResponse getProfile(String accountNumber, Authentication authentication) {

        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        Account account = accountRepository
                .getAccountByAccountNumber(accountNumber);

        if (!account.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException(
                    "Account does not belong to the authenticated user"
            );
        }

        return userMapper.mapProfileToResponse(account);
    }

    @Transactional
    public UserUpdateResponseDto updateUser(UserUpdateRequestDto userUpdateRequestDto,
                                            Authentication authentication) {

        //The either JSON fields should exists or just throw an exception
        if (userUpdateRequestDto.getName() == null && userUpdateRequestDto.getEmail() == null) {
            throw new IllegalArgumentException(
                    "Both fields shouldn't be empty"
            );
        }

        //Extract Email from the authentication object
        String currentEmail = authentication.getName();

        //Get the user by email
        User user = userRepository.findByEmail(currentEmail)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found"));

        //Check if the user has the same email as he sent in the request body
        if (userUpdateRequestDto.getEmail() != null &&
                userRepository.existsByEmailAndIdNot(
                        userUpdateRequestDto.getEmail(),
                        user.getId())) {
            throw new IllegalArgumentException("Email already exists");
        }

        if (userUpdateRequestDto.getName() != null) {
            user.setName(userUpdateRequestDto.getName());
        }

        if (userUpdateRequestDto.getEmail() != null) {
            user.setEmail(userUpdateRequestDto.getEmail());
        }

        return new UserUpdateResponseDto(
                user.getName(),
                user.getEmail()
        );
    }

}
