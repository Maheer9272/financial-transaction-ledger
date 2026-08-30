package com.maheer9272.LedgerCore.service;

import com.maheer9272.LedgerCore.dto.AccountResponseDto;
import com.maheer9272.LedgerCore.dto.UserProfileResponse;
import com.maheer9272.LedgerCore.entity.Account;
import com.maheer9272.LedgerCore.entity.User;
import com.maheer9272.LedgerCore.repository.AccountRepository;
import org.springframework.stereotype.Service;

import javax.security.auth.login.AccountNotFoundException;

@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /*
    This creates the account only once when the user registers for the first time, also used
    save and flush because, we we are returning the account number in user response dto so,
    it's the timing problem of dto creation and insert query ran by hibernate,
    as we are making a accessing dto before hibernate actually flushes immediately
    so we are using the saveAndFlush to flush immediately from persistence context to DB,
    but the excess use of save and flush in transactions would lead to performance slowdown
    */
    public Account createDefaultAccount(User user){
        Account account = new Account(user);
        accountRepository.saveAndFlush(account);
        return account;
    }

    public Account getAccountByAccountNumber(String accountNumber) {
        return accountRepository.getAccountByAccountNumber(accountNumber);
    }


}
