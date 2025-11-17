package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.request.login.LoginDTO;
import com.spring.digital_logistics.dto.request.user.AdminUserCreateDTO;
import com.spring.digital_logistics.dto.request.user.UserCreateDTO;
import com.spring.digital_logistics.dto.response.login.LoginResponseDTO;
import com.spring.digital_logistics.dto.response.user.UserDTO;
import com.spring.digital_logistics.entity.User;
import com.spring.digital_logistics.entity.enums.Role;
import com.spring.digital_logistics.exception.EmailAlreadyUsedException;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.mapper.UserMapper;
import com.spring.digital_logistics.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserDTO userDTO;
    private UserCreateDTO userCreateDTO;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setPassword("hashedPassword");
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setRole(Role.CLIENT);
        user.setActive(true);

        userDTO = new UserDTO();
        userDTO.setId(1L);
        userDTO.setEmail("test@test.com");
        userDTO.setFirstName("John");
        userDTO.setLastName("Doe");

        userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmail("test@test.com");
        userCreateDTO.setPassword("password123");
        userCreateDTO.setFirstName("John");
        userCreateDTO.setLastName("Doe");
    }

    // ========== TESTS register ==========

    @Test
    void register_withValidData_shouldCreateUser() {
        when(userRepository.findByEmail(userCreateDTO.getEmail())).thenReturn(Optional.empty());
        when(userMapper.toUser(userCreateDTO)).thenReturn(user);
        when(passwordEncoder.encode(userCreateDTO.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.register(userCreateDTO);

        assertNotNull(result);
        assertEquals(userDTO.getEmail(), result.getEmail());
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_withExistingEmail_shouldThrowException() {
        when(userRepository.findByEmail(userCreateDTO.getEmail())).thenReturn(Optional.of(user));

        assertThrows(EmailAlreadyUsedException.class,
                () -> userService.register(userCreateDTO));
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_shouldSetClientRoleAndActiveStatus() {
        when(userRepository.findByEmail(userCreateDTO.getEmail())).thenReturn(Optional.empty());
        when(userMapper.toUser(userCreateDTO)).thenReturn(user);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        userService.register(userCreateDTO);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        User savedUser = userCaptor.getValue();
        assertEquals(Role.CLIENT, savedUser.getRole());
        assertTrue(savedUser.isActive());
    }

    // ========== TESTS login ==========

    @Test
    void login_withValidCredentials_shouldReturnToken() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("test@test.com");
        loginDTO.setPassword("password123");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(null);
        when(userRepository.findByEmail(loginDTO.getEmail())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token-123");

        LoginResponseDTO result = userService.login(loginDTO);

        assertNotNull(result);
        assertEquals("jwt-token-123", result.getToken());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtService).generateToken(user);
    }

    // ========== TESTS getUserByEmail ==========

    @Test
    void getUserByEmail_withExistingEmail_shouldReturnUser() {
        when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.of(user));
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        Optional<UserDTO> result = userService.getUserByEmail("test@test.com");

        assertTrue(result.isPresent());
        assertEquals(userDTO.getEmail(), result.get().getEmail());
        verify(userRepository).findByEmail("test@test.com");
    }

    @Test
    void getUserByEmail_withNonExistingEmail_shouldThrowException() {
        when(userRepository.findByEmail("notfound@test.com")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserByEmail("notfound@test.com"));
    }

    // ========== TESTS getAllUsers ==========

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        User user2 = new User();
        user2.setId(2L);
        user2.setEmail("user2@test.com");

        UserDTO userDTO2 = new UserDTO();
        userDTO2.setId(2L);
        userDTO2.setEmail("user2@test.com");

        when(userRepository.findAll()).thenReturn(List.of(user, user2));
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);
        when(userMapper.toUserDTO(user2)).thenReturn(userDTO2);

        List<UserDTO> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    // ========== TESTS createUserByAdmin ==========

    @Test
    void createUserByAdmin_withValidData_shouldCreateUser() {
        AdminUserCreateDTO createDTO = new AdminUserCreateDTO();
        createDTO.setEmail("admin@test.com");
        createDTO.setPassword("password123");
        createDTO.setFirstName("Admin");
        createDTO.setLastName("User");
        createDTO.setRole(Role.ADMIN);

        when(userRepository.findByEmail(createDTO.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(createDTO.getPassword())).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.createUserByAdmin(createDTO);

        assertNotNull(result);
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUserByAdmin_withExistingEmail_shouldThrowException() {
        AdminUserCreateDTO createDTO = new AdminUserCreateDTO();
        createDTO.setEmail("existing@test.com");

        when(userRepository.findByEmail(createDTO.getEmail())).thenReturn(Optional.of(user));

        assertThrows(EmailAlreadyUsedException.class,
                () -> userService.createUserByAdmin(createDTO));
        verify(userRepository, never()).save(any());
    }

    // ========== TESTS updateUserStatus ==========

    @Test
    void updateUserStatus_shouldUpdateActiveStatus() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserDTO(user)).thenReturn(userDTO);

        UserDTO result = userService.updateUserStatus(1L, false);

        assertNotNull(result);
        assertFalse(user.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void updateUserStatus_withNonExistingUser_shouldThrowException() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUserStatus(999L, true));
    }

    // ========== TESTS deleteUser ==========

    @Test
    void deleteUser_withExistingId_shouldDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);
        doNothing().when(userRepository).deleteById(1L);

        userService.deleteUser(1L);

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteUser_withNonExistingId_shouldThrowException() {
        when(userRepository.existsById(999L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class,
                () -> userService.deleteUser(999L));
        verify(userRepository, never()).deleteById(any());
    }
}