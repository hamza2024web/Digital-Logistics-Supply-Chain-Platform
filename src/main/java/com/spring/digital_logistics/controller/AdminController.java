package com.spring.digital_logistics.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/hello")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<String> sayHelloToAdmin(){
        return ResponseEntity.ok("Bonjour, Administrateur ! Si vous voyez ce message, votre token est valide et vous avez le bon rôle.");
    }
}
