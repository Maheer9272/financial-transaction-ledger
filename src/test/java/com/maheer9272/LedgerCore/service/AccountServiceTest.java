package com.maheer9272.LedgerCore.service;

import com.maheer9272.LedgerCore.dto.AccountResponseDto;
import com.maheer9272.LedgerCore.entity.Account;
import com.maheer9272.LedgerCore.entity.User;
import com.maheer9272.LedgerCore.exception.ResourceDeniedException;
import com.maheer9272.LedgerCore.mapper.AccountMapper;
import com.maheer9272.LedgerCore.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;
    @Mock
    private AccountMapper accountMapper;
    @Mock
    private CurrentUserResolver currentUserResolver;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private AccountService accountService;


    @Test
    void shouldCreateDefaultAccountSuccessfully() {

        User user = new User(
                "Test User",
                "test@example.com",
                "password"
        );
        accountService.createDefaultAccount(user);
        verify(accountRepository)
                .saveAndFlush(any(Account.class));
    }


    @Test
    void shouldGetAccountSuccessfully() {

        String accountNumber = "12345678";
        User user = new User(
                "Test User",
                "test@example.com",
                "password"
        );
        Account account = new Account(user);
        AccountResponseDto responseDto = mock(AccountResponseDto.class);

        when(currentUserResolver.resolve(authentication))
                .thenReturn(user);
        when(accountRepository.findByAccountNumberAndUserId(
                eq(accountNumber),
                nullable(UUID.class)))
                .thenReturn(Optional.of(account));
        when(accountMapper.mapToResponse(account))
                .thenReturn(responseDto);

        AccountResponseDto result =
                accountService.getAccountByAccountNumber(
                        accountNumber,
                        authentication
                );

        assertSame(responseDto, result);
        verify(accountMapper)
                .mapToResponse(account);
    }


    @Test
    void shouldRejectAccountThatDoesNotBelongToUser() {

        String accountNumber = "12345678";
        User user = new User(
                "Test User",
                "test@example.com",
                "password"
        );

        when(currentUserResolver.resolve(authentication))
                .thenReturn(user);
        when(accountRepository.findByAccountNumberAndUserId(
                eq(accountNumber),
                nullable(UUID.class)))
                .thenReturn(Optional.empty());

        assertThrows(
                ResourceDeniedException.class,
                () -> accountService.getAccountByAccountNumber(
                        accountNumber,
                        authentication
                )
        );
        verify(accountMapper, never())
                .mapToResponse(any(Account.class));
    }


    @Test
    void shouldGetAllAccountsSuccessfully() {

        User user = new User(
                "Test User",
                "test@example.com",
                "password"
        );
        Account account1 = new Account(user);
        Account account2 = new Account(user);
        List<Account> accounts =
                List.of(account1, account2);
        List<AccountResponseDto> responseDtos =
                List.of(
                        mock(AccountResponseDto.class),
                        mock(AccountResponseDto.class)
                );

        when(currentUserResolver.resolve(authentication))
                .thenReturn(user);
        when(accountRepository.findByUserId(nullable(UUID.class)))
                .thenReturn(accounts);
        when(accountMapper.mapAccountsToDto(accounts))
                .thenReturn(responseDtos);

        List<AccountResponseDto> result =
                accountService.getAllAccountsByEmail(authentication);

        assertEquals(2, result.size());
        assertSame(responseDtos, result);
        verify(accountRepository)
                .findByUserId(nullable(UUID.class));
        verify(accountMapper)
                .mapAccountsToDto(accounts);
    }


    @Test
    void shouldReturnEmptyListWhenUserHasNoAccounts() {

        User user = new User(
                "Test User",
                "test@example.com",
                "password"
        );
        List<Account> emptyAccounts = List.of();
        List<AccountResponseDto> emptyResponse = List.of();

        when(currentUserResolver.resolve(authentication))
                .thenReturn(user);
        when(accountRepository.findByUserId(nullable(UUID.class)))
                .thenReturn(emptyAccounts);
        when(accountMapper.mapAccountsToDto(emptyAccounts))
                .thenReturn(emptyResponse);

        List<AccountResponseDto> result =
                accountService.getAllAccountsByEmail(authentication);

        assertTrue(result.isEmpty());
        verify(accountMapper)
                .mapAccountsToDto(emptyAccounts);
    }


    @Test
    void shouldCreateAdditionalAccountSuccessfully() {

        User user = new User(
                "Test User",
                "test@example.com",
                "password"
        );

        AccountResponseDto responseDto =
                mock(AccountResponseDto.class);

        when(currentUserResolver.resolve(authentication))
                .thenReturn(user);
        when(accountMapper.mapToResponse(any(Account.class)))
                .thenReturn(responseDto);

        AccountResponseDto result =
                accountService.createAdditionalAccount(authentication);

        assertSame(responseDto, result);
        verify(accountRepository)
                .saveAndFlush(any(Account.class));
        verify(accountMapper)
                .mapToResponse(any(Account.class));
    }
}