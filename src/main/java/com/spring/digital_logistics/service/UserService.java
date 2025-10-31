package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.request.LoginDTO;
import com.spring.digital_logistics.dto.request.UserCreateDTO;
import com.spring.digital_logistics.dto.response.UserDTO;
import com.spring.digital_logistics.entity.User;
import com.spring.digital_logistics.entity.enums.Role;
import com.spring.digital_logistics.exception.EmailAlreadyUsedException;
import com.spring.digital_logistics.exception.UserNotFoundException;
import com.spring.digital_logistics.mapper.UserMapper;
import com.spring.digital_logistics.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
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

        User user = userMapper.toUser(userCreateDTO);

        String hashedPassword = passwordEncoder.encode(userCreateDTO.getPassword());
        user.setPassword(hashedPassword);

        user.setRole(Role.CLIENT);

        user.setActive(true);

        User savedUser = userRepository.save(user);

        return userMapper.toUserDTO(savedUser);
    }

    public UserDTO login(LoginDTO loginDTO){
        User user = userRepository.findByEmail(loginDTO.getEmail()).orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé"));

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        if (!encoder.matches(loginDTO.getPassword(), user.getPassword())){
            throw new BadCredentialsException("Mot De Passe incorrect");
        }
        return userMapper.toUserDTO(user);
    }

    public Optional<UserDTO> getUserByEmail(String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec l'email : " + email));
        return Optional.of(userMapper.toUserDTO(user));
    }


}
