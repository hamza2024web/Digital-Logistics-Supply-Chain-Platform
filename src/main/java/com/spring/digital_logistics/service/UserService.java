package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.UserCreateDTO;
import com.spring.digital_logistics.dto.UserDTO;
import com.spring.digital_logistics.entity.User;
import com.spring.digital_logistics.mapper.UserMapper;
import com.spring.digital_logistics.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UserRepository userRepository , UserMapper userMapper){
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserDTO createUser(UserCreateDTO userCreateDTO){
        User user = userMapper.toUser(userCreateDTO);
        User savedUser = userRepository.save(user);
        return userMapper.toUserDTO(savedUser);
    }

    public Optional<UserDTO> getUserByEmail(String email){
        return userRepository.findByEmail(email).map(userMapper::toUserDTO);
    }


}
