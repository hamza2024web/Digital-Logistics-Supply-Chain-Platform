package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.UserCreateDTO;
import com.spring.digital_logistics.dto.UserDTO;
import com.spring.digital_logistics.entity.User;
import com.spring.digital_logistics.exception.EmailAlreadyUsedException;
import com.spring.digital_logistics.exception.UserNotFoundException;
import com.spring.digital_logistics.mapper.UserMapper;
import com.spring.digital_logistics.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository , UserMapper userMapper){
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @Transactional
    public UserDTO register(UserCreateDTO userCreateDTO){
        if (userRepository.findByEmail(userCreateDTO.getEmail()).isPresent()){
            throw new EmailAlreadyUsedException("Cet email est déjà utilisé !");
        }
    }

    public Optional<UserDTO> getUserByEmail(String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec l'email : " + email));
        return Optional.of(userMapper.toUserDTO(user));
    }


}
