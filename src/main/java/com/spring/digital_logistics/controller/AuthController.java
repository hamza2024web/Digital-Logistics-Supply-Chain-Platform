package com.spring.digital_logistics.controller;

import com.spring.digital_logistics.dto.request.UserCreateDTO;
import com.spring.digital_logistics.dto.response.UserDTO;
import com.spring.digital_logistics.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController (UserService userService){
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody UserCreateDTO userCreateDTO){
        UserDTO createUser = userService.register(userCreateDTO);
        return ResponseEntity.ok(createUser);
    }
}
