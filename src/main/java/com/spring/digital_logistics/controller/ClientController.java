package com.spring.digital_logistics.controller;

import com.spring.digital_logistics.dto.request.salesOrder.SalesOrderCreateDTO;
import com.spring.digital_logistics.dto.response.salesOrder.SalesOrderDTO;
import com.spring.digital_logistics.entity.User;
import com.spring.digital_logistics.service.SalesOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    private final SalesOrderService salesOrderService;

    public ClientController(SalesOrderService salesOrderService){
        this.salesOrderService = salesOrderService;
    }

    @PostMapping("/orders")
    @PreAuthorize("hasAuthority('CLIENT')")
    public ResponseEntity<SalesOrderDTO> createSalesOrder(@Valid @RequestBody SalesOrderCreateDTO createDTO , @AuthenticationPrincipal User currentUser){
        SalesOrderDTO newOrder = salesOrderService.createOrder(createDTO , currentUser);
        return new ResponseEntity<>(newOrder , HttpStatus.CREATED);
    }

    @PatchMapping("/orders/{orderId}/reserve")
    @PreAuthorize("hasAuthority('CLIENT')")
    public ResponseEntity<SalesOrderDTO> reserveOrderStock(@PathVariable Long orderId, @AuthenticationPrincipal User currentUser) {
        SalesOrderDTO reservedOrder = salesOrderService.reserveOrderStock(orderId, currentUser);
        return ResponseEntity.ok(reservedOrder);
    }
}
