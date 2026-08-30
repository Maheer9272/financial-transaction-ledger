package com.maheer9272.LedgerCore.service;

import com.maheer9272.LedgerCore.dto.*;
import com.maheer9272.LedgerCore.entity.Account;
import com.maheer9272.LedgerCore.entity.User;
import com.maheer9272.LedgerCore.mapper.AccountMapper;
import com.maheer9272.LedgerCore.mapper.UserMapper;
import com.maheer9272.LedgerCore.repository.AccountRepository;
import com.maheer9272.LedgerCore.repository.UserRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AccountService accountService;
    private final AccountMapper accountMapper;
    private final AccountRepository accountRepository;

    public UserService(UserRepository userRepository, UserMapper userMapper, AccountService accountService, AccountMapper accountMapper, AccountRepository accountRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.accountService = accountService;
        this.accountMapper = accountMapper;
        this.accountRepository = accountRepository;
    }

    /*
    * This method create the user and a one default bank account
    * */
    @Transactional
    public CreateUserResponseDto createUser(CreateUserRequestDto requestDto){
        User user = userMapper.mapToUser(requestDto);
        userRepository.save(user);
        Account account = accountService.createDefaultAccount(user);
        return userMapper.mapToResponse(user,account);
    }

    @Transactional
    public UserProfileResponse getProfile(String accountNumber){
        Account account = accountRepository.getAccountByAccountNumber(accountNumber);
        return userMapper.mapProfileToResponse(account);
    }

}
