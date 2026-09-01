package com.maheer9272.LedgerCore.service;

import com.maheer9272.LedgerCore.dto.AccountResponseDto;
import com.maheer9272.LedgerCore.entity.Account;
import com.maheer9272.LedgerCore.entity.User;
import com.maheer9272.LedgerCore.mapper.AccountMapper;
import com.maheer9272.LedgerCore.repository.AccountRepository;
import com.maheer9272.LedgerCore.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final AccountMapper accountMapper;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository, AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.accountMapper = accountMapper;
    }

    /*
    This creates the account only once when the user registers for the first time, also used
    save and flush because, we we are returning the account number in user response dto so,
    it's the timing problem of dto creation and insert query ran by hibernate,
    as we are making a accessing dto before hibernate actually flushes immediately
    so we are using the saveAndFlush to flush immediately from persistence context to DB,
    but the excess use of save and flush in transactions would lead to performance slowdown
    */
    @Transactional
    public Account createDefaultAccount(User user) {
        Account account = new Account(user);
        accountRepository.saveAndFlush(account);
        return account;
    }

    @Transactional
    public AccountResponseDto getAccountByAccountNumber(String accountNumber, Authentication authentication) {
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Email does not exist"));

        Account account = accountRepository
                .findByAccountNumberAndUserId(
                        accountNumber,
                        user.getId())
                .orElseThrow(() ->
                        new IllegalArgumentException("This account doesn't belong to you"));

        return accountMapper.mapToResponse(account);
    }


    @Transactional
    public List<AccountResponseDto> getAllAccountsByEmail(
            Authentication authentication) {

        //Email of the user
        String email = authentication.getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("Email does not exist"));

        List<Account> accountList = accountRepository.findByUserId(user.getId());

        return accountMapper.mapAccountsToDto(accountList);
    }
}
