package com.maheer9272.LedgerCore.service;

import com.maheer9272.LedgerCore.dto.CreateUserRequestDto;
import com.maheer9272.LedgerCore.dto.CreateUserResponseDto;
import com.maheer9272.LedgerCore.dto.LoginRequestDto;
import com.maheer9272.LedgerCore.dto.LoginResponseDto;
import com.maheer9272.LedgerCore.entity.Account;
import com.maheer9272.LedgerCore.entity.User;
import com.maheer9272.LedgerCore.mapper.UserMapper;
import com.maheer9272.LedgerCore.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AccountService accountService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, UserMapper userMapper, AccountService accountService, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtService jwtService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    /*
     * This method create the user and a one default bank account
     * */
    @Transactional
    public CreateUserResponseDto register(CreateUserRequestDto requestDto) {
        //Encode the password before creating a User object with raw password
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());

        //Using the constructor instead of any setter because ofc public setters that too of entity that's a security flaw
        User user = new User(
                requestDto.getName(),
                requestDto.getEmail(),
                encodedPassword
        );

        userRepository.save(user);
        Account account = accountService.createDefaultAccount(user);

        return userMapper.mapToResponse(user, account);
    }

    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        Authentication authenticationRequest =
                UsernamePasswordAuthenticationToken.unauthenticated(
                        loginRequestDto.getEmail(),
                        loginRequestDto.getPassword()
                );

        Authentication authentication = authenticationManager.authenticate(authenticationRequest);

        String token = jwtService.generateToken(authentication);

        return new LoginResponseDto(token);
    }

}
