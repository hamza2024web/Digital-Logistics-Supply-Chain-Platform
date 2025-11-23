package com.spring.digital_logistics.controller;

import com.spring.digital_logistics.dto.request.user.UserCreateDTO;
import com.spring.digital_logistics.dto.response.user.UserDTO;
import com.spring.digital_logistics.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/basic/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserDTO> getAllUsers(@RequestBody UserCreateDTO userCreateDTO){
        UserDTO createUser = userService.register(userCreateDTO);
        return ResponseEntity.ok(createUser);
    }

    @GetMapping("/by-email")
    public ResponseEntity<UserDTO> getUserByEmail(@RequestParam String email){
        return userService.getUserByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
