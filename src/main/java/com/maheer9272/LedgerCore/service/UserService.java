package com.maheer9272.LedgerCore.service;

import com.maheer9272.LedgerCore.dto.CreateUserRequestDto;
import com.maheer9272.LedgerCore.dto.CreateUserResponseDto;
import com.maheer9272.LedgerCore.entity.User;
import com.maheer9272.LedgerCore.mapper.UserMapper;
import com.maheer9272.LedgerCore.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    public CreateUserResponseDto createUser(CreateUserRequestDto requestDto){
        User user = userMapper.mapToUser(requestDto);
        userRepository.save(user);
        return userMapper.mapToResponse(user);
    }
}
