package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.request.user.AdminUserCreateDTO;
import com.spring.digital_logistics.dto.request.login.LoginDTO;
import com.spring.digital_logistics.dto.request.user.UserCreateDTO;
import com.spring.digital_logistics.dto.response.login.LoginResponseDTO;
import com.spring.digital_logistics.dto.response.user.UserDTO;
import com.spring.digital_logistics.entity.RefreshToken;
import com.spring.digital_logistics.entity.User;
import com.spring.digital_logistics.entity.enums.Role;
import com.spring.digital_logistics.exception.EmailAlreadyUsedException;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.mapper.UserMapper;
import com.spring.digital_logistics.repository.UserRepository;
import com.spring.digital_logistics.security.jwt.JwtUtils;
import com.spring.digital_logistics.security.service.UserDetailsImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final JwtUtils jwtUtils;
    private final RefreshTokenService refreshTokenService;

    @Autowired
    public UserService(UserRepository userRepository , UserMapper userMapper, PasswordEncoder passwordEncoder, JwtService jwtService, AuthenticationManager authenticationManager, JwtUtils jwtUtils, RefreshTokenService refreshTokenService){
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.refreshTokenService = refreshTokenService;
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
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginDTO.getEmail(),loginDTO.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        String jwt = jwtUtils.generateJwtToken(authentication);

        refreshTokenService.deleteByUserId(userDetails.getId());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(userDetails.getId());

        return LoginResponseDTO.builder()
                .token(jwt)
                .refreshToken(refreshToken.getToken())
                .email(userDetails.getEmail())
                .role(userDetails.getAuthorities().stream().findFirst().get().getAuthority())
                .build();
    }

    public Optional<UserDTO> getUserByEmail(String email){
        User user = userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'email : " + email));
        return Optional.of(userMapper.toUserDTO(user));
    }

    public List<UserDTO> getAllUsers(){
        List<User> users = userRepository.findAll();
        return users.stream().map(userMapper::toUserDTO).toList();
    }

    public UserDTO createUserByAdmin(AdminUserCreateDTO createDTO){
        if (userRepository.findByEmail(createDTO.getEmail()).isPresent()){
            throw new EmailAlreadyUsedException("Cet Email est déjà utilisé !");
        }

        User user = new User(createDTO.getFirstName(),createDTO.getLastName(),createDTO.getEmail(),createDTO.getRole());
        user.setPassword(passwordEncoder.encode(createDTO.getPassword()));

        User savedUser = userRepository.save(user);

        return userMapper.toUserDTO(savedUser);
    }

    public UserDTO updateUserStatus(Long userId, boolean isActive){
        User user = userRepository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé avec l'ID : " + userId));

        user.setActive(isActive);
        User updatedUser = userRepository.save(user);

        return userMapper.toUserDTO(updatedUser);
    }

    @Transactional
    public void deleteUser(Long userId){
        if (!userRepository.existsById(userId)){
            throw new ResourceNotFoundException("Utilisateur non trouvé avec l'ID : " + userId);
        }

        userRepository.deleteById(userId);
    }
}
