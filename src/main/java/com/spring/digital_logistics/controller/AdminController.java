package com.spring.digital_logistics.controller;

import com.spring.digital_logistics.dto.request.AdminUserCreateDTO;
import com.spring.digital_logistics.dto.response.UserDTO;
import com.spring.digital_logistics.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final UserService userService;

    public AdminController(UserService userService){
        this.userService = userService;
    }
    @GetMapping("/hello")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> sayHelloToAdmin(){
        return ResponseEntity.ok("Bonjour, Administrateur ! Si vous voyez ce message, votre token est valide et vous avez le bon rôle.");
    }

    @Operation(summary = "Obtenir Tous les Utilisateurs")
    @GetMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<List<UserDTO>> listAllUsers(){
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @Operation(summary = "Crée un utilisateur")
    @PostMapping("/users")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<UserDTO> createUser(@Valid @RequestBody AdminUserCreateDTO createDTO){
        UserDTO createdUser = userService.createUserByAdmin(createDTO);
        return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
    }
}
