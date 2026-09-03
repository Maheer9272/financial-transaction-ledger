package com.maheer9272.LedgerCore.service;

import com.maheer9272.LedgerCore.dto.UserProfileResponse;
import com.maheer9272.LedgerCore.dto.UserUpdateRequestDto;
import com.maheer9272.LedgerCore.dto.UserUpdateResponseDto;
import com.maheer9272.LedgerCore.entity.Account;
import com.maheer9272.LedgerCore.entity.User;
import com.maheer9272.LedgerCore.exception.DuplicateResourceException;
import com.maheer9272.LedgerCore.exception.ResourceDeniedException;
import com.maheer9272.LedgerCore.mapper.UserMapper;
import com.maheer9272.LedgerCore.repository.AccountRepository;
import com.maheer9272.LedgerCore.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AccountRepository accountRepository;
    private final CurrentUserResolver currentUserResolver;


    public UserService(UserRepository userRepository,
                       UserMapper userMapper,
                       AccountRepository accountRepository,
                       CurrentUserResolver currentUserResolver) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.accountRepository = accountRepository;
        this.currentUserResolver = currentUserResolver;
    }

    @Transactional
    public UserProfileResponse getProfile(String accountNumber, Authentication authentication) {

        User user = currentUserResolver.resolve(authentication);

        Account account = accountRepository
                .findByAccountNumberAndUserId(
                        accountNumber,
                        user.getId())
                .orElseThrow(() ->
                        new ResourceDeniedException("This account doesn't belong to you"));

        return userMapper.mapProfileToResponse(user,account);
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

        User user = currentUserResolver.resolve(authentication);

        //Check if the user has the same email as he sent in the request body
        if (userUpdateRequestDto.getEmail() != null &&
                userRepository.existsByEmailAndIdNot(
                        userUpdateRequestDto.getEmail(),
                        user.getId())) {
            throw new DuplicateResourceException("Email already exists");
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
