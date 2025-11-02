package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.request.AdminUserCreateDTO;
import com.spring.digital_logistics.dto.request.LoginDTO;
import com.spring.digital_logistics.dto.request.UserCreateDTO;
import com.spring.digital_logistics.dto.response.LoginResponseDTO;
import com.spring.digital_logistics.dto.response.UserDTO;
import com.spring.digital_logistics.entity.User;
import com.spring.digital_logistics.entity.enums.Role;
import com.spring.digital_logistics.exception.EmailAlreadyUsedException;
import com.spring.digital_logistics.exception.UserNotFoundException;
import com.spring.digital_logistics.mapper.UserMapper;
import com.spring.digital_logistics.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public UserService(UserRepository userRepository , UserMapper userMapper, PasswordEncoder passwordEncoder,JwtService jwtService, AuthenticationManager authenticationManager){
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
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

    public LoginResponseDTO login(LoginDTO loginDTO){
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getEmail(),loginDTO.getPassword()));

        User user = userRepository.findByEmail(loginDTO.getEmail()).orElseThrow();

        String jwtToken  = jwtService.generateToken(user);

        return new LoginResponseDTO(jwtToken);
    }

    public Optional<UserDTO> getUserByEmail(String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec l'email : " + email));
        return Optional.of(userMapper.toUserDTO(user));
    }

    public List<UserDTO> getAllUsers(){
        List<User> users = userRepository.findAll();
        return users.stream().map(userMapper::toUserDTO).collect(Collectors.toList());
    }

    public UserDTO createUserByAdmin(AdminUserCreateDTO createDTO){
        if (userRepository.findByEmail(createDTO.getEmail()).isPresent()){
            throw new EmailAlreadyUsedException("Cet Email est déjà utilisé !");
        }

        User user = new User();
        user.setFirstName(createDTO.getFirstName());
        user.setLastName(createDTO.getLastName());
        user.setEmail(createDTO.getEmail());
        user.setPassword(passwordEncoder.encode(createDTO.getPassword()));
        user.setRole(createDTO.getRole());
        user.setActive(true);

        User savedUser = userRepository.save(user);

        return userMapper.toUserDTO(savedUser);
    }

    public UserDTO updateUserStatus(Long userId, boolean isActive){
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("Utilisateur non trouvé avec l'ID : " + userId));

        user.setActive(isActive);
        User updatedUser = userRepository.save(user);

        return userMapper.toUserDTO(updatedUser);
    }
}
