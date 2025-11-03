package com.spring.digital_logistics.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/warehouse-manager")
public class WarehouseManagerController {

    @GetMapping("/hello")
    @PreAuthorize("hasAuthority('WAREHOUSE_MANAGER')")
    public ResponseEntity<String> sayHelloWarehouseManager(){
        return ResponseEntity.ok("Bonjour, Warehouse Manager ! Si vous voyez ce message, c'est que votre rôle est bien reconnu.");
    }
}
