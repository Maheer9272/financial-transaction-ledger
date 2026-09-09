package com.maheer9272.LedgerCore.service;

import com.maheer9272.LedgerCore.dto.UserProfileResponse;
import com.maheer9272.LedgerCore.dto.UserProfileResponseUsingAccountNumber;
import com.maheer9272.LedgerCore.dto.UserUpdateRequestDto;
import com.maheer9272.LedgerCore.dto.UserUpdateResponseDto;
import com.maheer9272.LedgerCore.entity.Account;
import com.maheer9272.LedgerCore.entity.User;
import com.maheer9272.LedgerCore.exception.DuplicateResourceException;
import com.maheer9272.LedgerCore.exception.ResourceDeniedException;
import com.maheer9272.LedgerCore.mapper.UserMapper;
import com.maheer9272.LedgerCore.repository.AccountRepository;
import com.maheer9272.LedgerCore.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private CurrentUserResolver currentUserResolver;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;


    @Test
    void shouldGetProfileUsingAccountNumberSuccessfully() {

        String accountNumber = "12345678";
        User user = new User(
                "Test User",
                "test@example.com",
                "password"
        );
        Account account = new Account(user);
        UserProfileResponseUsingAccountNumber responseDto =
                mock(UserProfileResponseUsingAccountNumber.class);

        when(currentUserResolver.resolve(authentication))
                .thenReturn(user);
        when(accountRepository.findByAccountNumberAndUserId(
                eq(accountNumber),
                nullable(UUID.class)))
                .thenReturn(Optional.of(account));
        when(userMapper.mapProfileToResponse(user, account))
                .thenReturn(responseDto);

        UserProfileResponseUsingAccountNumber result =
                userService.getProfileUsingAccountNumber(
                        accountNumber,
                        authentication
                );

        assertSame(responseDto, result);
        verify(userMapper)
                .mapProfileToResponse(user, account);
    }


    @Test
    void shouldRejectProfileWhenAccountDoesNotBelongToUser() {

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
                () -> userService.getProfileUsingAccountNumber(
                        accountNumber,
                        authentication
                )
        );
        verify(userMapper, never())
                .mapProfileToResponse(
                        any(User.class),
                        any(Account.class)
                );
    }


    @Test
    void shouldGetUserProfileSuccessfully() {
        User user = new User(
                "Test User",
                "test@example.com",
                "password"
        );

        when(currentUserResolver.resolve(authentication))
                .thenReturn(user);

        UserProfileResponse result =
                userService.getProfile(authentication);

        assertEquals(
                "Test User",
                result.getName()
        );
        assertEquals(
                "test@example.com",
                result.getEmail()
        );
    }


    @Test
    void shouldUpdateUserNameSuccessfully() {

        User user = new User(
                "Old Name",
                "test@example.com",
                "password"
        );

        UserUpdateRequestDto requestDto =
                new UserUpdateRequestDto(
                        "New Name",
                        null
                );

        when(currentUserResolver.resolve(authentication))
                .thenReturn(user);

        UserUpdateResponseDto result =
                userService.updateUser(
                        requestDto,
                        authentication
                );

        assertEquals(
                "New Name",
                result.getName()
        );

        assertEquals(
                "test@example.com",
                result.getEmail()
        );

        assertEquals(
                "New Name",
                user.getName()
        );

        verify(userRepository, never())
                .existsByEmailAndIdNot(
                        anyString(),
                        nullable(UUID.class)
                );
    }


    @Test
    void shouldUpdateUserEmailSuccessfully() {

        User user = new User(
                "Test User",
                "old@example.com",
                "password"
        );

        String newEmail = "new@example.com";

        UserUpdateRequestDto requestDto =
                new UserUpdateRequestDto(
                        null,
                        newEmail
                );

        when(currentUserResolver.resolve(authentication))
                .thenReturn(user);

        when(userRepository.existsByEmailAndIdNot(
                eq(newEmail),
                nullable(UUID.class)))
                .thenReturn(false);

        UserUpdateResponseDto result =
                userService.updateUser(
                        requestDto,
                        authentication
                );

        assertEquals(
                "Test User",
                result.getName()
        );
        assertEquals(
                newEmail,
                result.getEmail()
        );
        assertEquals(
                newEmail,
                user.getEmail()
        );
    }


    @Test
    void shouldUpdateNameAndEmailSuccessfully() {

        User user = new User(
                "Old Name",
                "old@example.com",
                "password"
        );
        String newEmail = "new@example.com";
        UserUpdateRequestDto requestDto =
                new UserUpdateRequestDto(
                        "New Name",
                        newEmail
                );

        when(currentUserResolver.resolve(authentication))
                .thenReturn(user);
        when(userRepository.existsByEmailAndIdNot(
                eq(newEmail),
                nullable(UUID.class)))
                .thenReturn(false);

        UserUpdateResponseDto result =
                userService.updateUser(
                        requestDto,
                        authentication
                );

        assertEquals(
                "New Name",
                result.getName()
        );
        assertEquals(
                newEmail,
                result.getEmail()
        );
        assertEquals(
                "New Name",
                user.getName()
        );
        assertEquals(
                newEmail,
                user.getEmail()
        );
    }


    @Test
    void shouldRejectUpdateWhenBothFieldsAreNull() {

        UserUpdateRequestDto requestDto =
                new UserUpdateRequestDto(
                        null,
                        null
                );

        assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateUser(
                        requestDto,
                        authentication
                )
        );

        verify(currentUserResolver, never())
                .resolve(authentication);
    }


    @Test
    void shouldRejectUpdateWhenEmailAlreadyExists() {
        User user = new User(
                "Test User",
                "old@example.com",
                "password"
        );
        String existingEmail = "existing@example.com";
        UserUpdateRequestDto requestDto =
                new UserUpdateRequestDto(
                        null,
                        existingEmail
                );

        when(currentUserResolver.resolve(authentication))
                .thenReturn(user);

        when(userRepository.existsByEmailAndIdNot(
                eq(existingEmail),
                nullable(UUID.class)))
                .thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> userService.updateUser(
                        requestDto,
                        authentication
                )
        );

        assertEquals(
                "old@example.com",
                user.getEmail()
        );
    }


    @Test
    void shouldAllowUserToKeepOwnEmail() {

        User user = new User(
                "Test User",
                "test@example.com",
                "password"
        );

        UserUpdateRequestDto requestDto =
                new UserUpdateRequestDto(
                        "Updated Name",
                        "test@example.com"
                );

        when(currentUserResolver.resolve(authentication))
                .thenReturn(user);
        when(userRepository.existsByEmailAndIdNot(
                eq("test@example.com"),
                nullable(UUID.class)))
                .thenReturn(false);

        UserUpdateResponseDto result =
                userService.updateUser(
                        requestDto,
                        authentication
                );

        assertEquals(
                "Updated Name",
                result.getName()
        );
        assertEquals(
                "test@example.com",
                result.getEmail()
        );
    }
}