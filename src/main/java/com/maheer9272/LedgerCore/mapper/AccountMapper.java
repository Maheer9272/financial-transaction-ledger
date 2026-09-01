package com.maheer9272.LedgerCore.mapper;

import com.maheer9272.LedgerCore.dto.AccountResponseDto;
import com.maheer9272.LedgerCore.entity.Account;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AccountMapper {

    public AccountResponseDto mapToResponse(Account account) {

        return new AccountResponseDto(
                account.getAccountNumber(),
                account.getBalance()
        );

    }

    public List<AccountResponseDto> mapAccountsToDto(List<Account> accountList){

        return accountList.stream()
                .map(account -> new AccountResponseDto(
                        account.getAccountNumber(),
                        account.getBalance()
                ))
                .toList();
    }
}
