package com.spring.digital_logistics.controller;

import com.spring.digital_logistics.dto.request.LoginDTO;
import com.spring.digital_logistics.dto.request.UserCreateDTO;
import com.spring.digital_logistics.dto.response.UserDTO;
import com.spring.digital_logistics.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController (UserService userService){
        this.userService = userService;
    }

    @Operation(summary = "Inscritpion d'un nouvel utilisateur ")
    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@Valid @RequestBody UserCreateDTO userCreateDTO){
        UserDTO createUser = userService.register(userCreateDTO);
        return ResponseEntity.ok(createUser);
    }

    @Operation(summary = "Authentification d'un utilisateur")
    @PostMapping("/login")
    public ResponseEntity<UserDTO> login(@Valid @RequestBody LoginDTO loginDTO){
        UserDTO userDTO = userService.login(loginDTO);
        return ResponseEntity.ok(userDTO);
    }
}
