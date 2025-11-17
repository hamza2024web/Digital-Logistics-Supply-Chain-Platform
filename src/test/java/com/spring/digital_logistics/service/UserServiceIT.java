package com.spring.digital_logistics.service;

import com.spring.digital_logistics.IntegrationTestBase;
import com.spring.digital_logistics.dto.request.login.LoginDTO;
import com.spring.digital_logistics.dto.request.user.AdminUserCreateDTO;
import com.spring.digital_logistics.dto.request.user.UserCreateDTO;
import com.spring.digital_logistics.dto.response.login.LoginResponseDTO;
import com.spring.digital_logistics.dto.response.user.UserDTO;
import com.spring.digital_logistics.entity.User;
import com.spring.digital_logistics.entity.enums.Role;
import com.spring.digital_logistics.exception.EmailAlreadyUsedException;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class UserServiceIT extends IntegrationTestBase {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ========== TESTS register ==========

    @Test
    void register_withValidData_shouldSaveToDatabase() {
        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setEmail("newuser@test.com");
        createDTO.setPassword("password123");
        createDTO.setFirstName("John");
        createDTO.setLastName("Doe");

        UserDTO result = userService.register(createDTO);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("newuser@test.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals(Role.CLIENT, result.getRole());
        assertTrue(result.isActive());

        User savedUser = userRepository.findById(result.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword()));
    }

    @Test
    void register_withExistingEmail_shouldThrowException() {
        userRepository.save(createUser("existing@test.com"));

        UserCreateDTO createDTO = new UserCreateDTO();
        createDTO.setEmail("existing@test.com");
        createDTO.setPassword("password123");
        createDTO.setFirstName("Test");
        createDTO.setLastName("User");

        assertThrows(EmailAlreadyUsedException.class,
                () -> userService.register(createDTO));
    }

    // ========== TESTS login ==========

    @Test
    void login_withValidCredentials_shouldReturnToken() {
        User user = createUser("login@test.com");
        user.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(user);

        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("login@test.com");
        loginDTO.setPassword("password123");

        LoginResponseDTO result = userService.login(loginDTO);

        assertNotNull(result);
        assertNotNull(result.getToken());
        assertFalse(result.getToken().isEmpty());
    }

    // ========== TESTS getUserByEmail ==========

    @Test
    void getUserByEmail_withExistingEmail_shouldReturnUser() {
        User user = userRepository.save(createUser("find@test.com"));

        Optional<UserDTO> result = userService.getUserByEmail("find@test.com");

        assertTrue(result.isPresent());
        assertEquals("find@test.com", result.get().getEmail());
        assertEquals(user.getId(), result.get().getId());
    }

    @Test
    void getUserByEmail_withNonExistingEmail_shouldThrowException() {
        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserByEmail("notfound@test.com"));
    }

    // ========== TESTS getAllUsers ==========

    @Test
    void getAllUsers_shouldReturnAllUsers() {
        userRepository.save(createUser("user1@test.com"));
        userRepository.save(createUser("user2@test.com"));
        userRepository.save(createUser("user3@test.com"));

        List<UserDTO> users = userService.getAllUsers();

        assertNotNull(users);
        assertEquals(3, users.size());
    }

    @Test
    void getAllUsers_whenNoUsers_shouldReturnEmptyList() {
        List<UserDTO> users = userService.getAllUsers();

        assertNotNull(users);
        assertTrue(users.isEmpty());
    }

    // ========== TESTS createUserByAdmin ==========

    @Test
    void createUserByAdmin_withValidData_shouldCreateUser() {
        AdminUserCreateDTO createDTO = new AdminUserCreateDTO();
        createDTO.setEmail("admin@test.com");
        createDTO.setPassword("adminPass123");
        createDTO.setFirstName("Admin");
        createDTO.setLastName("User");
        createDTO.setRole(Role.ADMIN);

        UserDTO result = userService.createUserByAdmin(createDTO);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("admin@test.com", result.getEmail());
        assertEquals(Role.ADMIN, result.getRole());

        User savedUser = userRepository.findById(result.getId()).orElseThrow();
        assertTrue(passwordEncoder.matches("adminPass123", savedUser.getPassword()));
    }

    @Test
    void createUserByAdmin_withExistingEmail_shouldThrowException() {
        userRepository.save(createUser("existing@admin.com"));

        AdminUserCreateDTO createDTO = new AdminUserCreateDTO();
        createDTO.setEmail("existing@admin.com");
        createDTO.setPassword("password");
        createDTO.setFirstName("Test");
        createDTO.setLastName("User");
        createDTO.setRole(Role.WAREHOUSE_MANAGER);

        assertThrows(EmailAlreadyUsedException.class,
                () -> userService.createUserByAdmin(createDTO));
    }

    @Test
    void createUserByAdmin_shouldSupportDifferentRoles() {
        AdminUserCreateDTO managerDTO = new AdminUserCreateDTO();
        managerDTO.setEmail("manager@test.com");
        managerDTO.setPassword("password");
        managerDTO.setFirstName("Manager");
        managerDTO.setLastName("User");
        managerDTO.setRole(Role.WAREHOUSE_MANAGER);

        UserDTO result = userService.createUserByAdmin(managerDTO);

        assertEquals(Role.WAREHOUSE_MANAGER, result.getRole());
    }

    // ========== TESTS updateUserStatus ==========

    @Test
    void updateUserStatus_shouldUpdateActiveFlag() {
        User user = userRepository.save(createUser("status@test.com"));
        assertTrue(user.isActive());

        UserDTO result = userService.updateUserStatus(user.getId(), false);

        assertFalse(result.isActive());

        User updatedUser = userRepository.findById(user.getId()).orElseThrow();
        assertFalse(updatedUser.isActive());
    }

    @Test
    void updateUserStatus_withNonExistingUser_shouldThrowException() {
        assertThrows(ResourceNotFoundException.class,
                () -> userService.updateUserStatus(9999L, true));
    }

    @Test
    void updateUserStatus_shouldToggleMultipleTimes() {
        User user = userRepository.save(createUser("toggle@test.com"));

        userService.updateUserStatus(user.getId(), false);
        User updated1 = userRepository.findById(user.getId()).orElseThrow();
        assertFalse(updated1.isActive());

        userService.updateUserStatus(user.getId(), true);
        User updated2 = userRepository.findById(user.getId()).orElseThrow();
        assertTrue(updated2.isActive());
    }

    // ========== TESTS deleteUser ==========

    @Test
    void deleteUser_withExistingId_shouldDeleteFromDatabase() {
        User user = userRepository.save(createUser("delete@test.com"));
        Long userId = user.getId();

        assertTrue(userRepository.existsById(userId));

        userService.deleteUser(userId);

        assertFalse(userRepository.existsById(userId));
    }

    @Test
    void deleteUser_withNonExistingId_shouldThrowException() {
        assertThrows(ResourceNotFoundException.class,
                () -> userService.deleteUser(9999L));
    }

    @Test
    void deleteUser_shouldNotAffectOtherUsers() {
        User user1 = userRepository.save(createUser("user1@delete.com"));
        User user2 = userRepository.save(createUser("user2@delete.com"));

        userService.deleteUser(user1.getId());

        assertFalse(userRepository.existsById(user1.getId()));
        assertTrue(userRepository.existsById(user2.getId()));
    }

    // ========== Helper Methods ==========

    private User createUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(Role.CLIENT);
        user.setActive(true);
        return user;
    }
}