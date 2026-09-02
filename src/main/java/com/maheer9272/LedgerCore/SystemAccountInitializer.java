package com.maheer9272.LedgerCore;

import com.maheer9272.LedgerCore.entity.Account;
import com.maheer9272.LedgerCore.entity.AccountType;
import com.maheer9272.LedgerCore.repository.AccountRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SystemAccountInitializer implements ApplicationRunner {

    private final AccountRepository accountRepository;

    public SystemAccountInitializer(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Override
    public void run(ApplicationArguments args) {

        if (accountRepository
                .findByAccountType(AccountType.SYSTEM)
                .isEmpty()) {

            Account systemAccount =
                    Account.createSystemAccount(new BigDecimal("1000000.0000"));

            accountRepository.save(systemAccount);
        }
    }
}