package com.spring.digital_logistics.controller;

import com.spring.digital_logistics.dto.request.salesOrder.SalesOrderCreateDTO;
import com.spring.digital_logistics.dto.response.salesOrder.SalesOrderDTO;
import com.spring.digital_logistics.entity.User;
import com.spring.digital_logistics.security.service.UserDetailsImpl;
import com.spring.digital_logistics.service.SalesOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    private final SalesOrderService salesOrderService;

    public ClientController(SalesOrderService salesOrderService){
        this.salesOrderService = salesOrderService;
    }

    @PostMapping("/orders")
    @PreAuthorize("hasAuthority('CLIENT')")
    public ResponseEntity<SalesOrderDTO> createSalesOrder(@Valid @RequestBody SalesOrderCreateDTO createDTO , @AuthenticationPrincipal UserDetailsImpl userDetails){
        User client = userDetails.getUser();
        SalesOrderDTO newOrder = salesOrderService.createOrder(createDTO , client);
        return new ResponseEntity<>(newOrder , HttpStatus.CREATED);
    }

    @PatchMapping("/orders/{orderId}/reserve")
    @PreAuthorize("hasAuthority('CLIENT')")
    public ResponseEntity<SalesOrderDTO> reserveOrderStock(@PathVariable Long orderId, @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User client = userDetails.getUser();
        SalesOrderDTO reservedOrder = salesOrderService.reserveOrderStock(orderId, client);
        return ResponseEntity.ok(reservedOrder);
    }

    @GetMapping("/orders")
    @PreAuthorize("hasAuthority('CLIENT')")
    public ResponseEntity<List<SalesOrderDTO>> getOrders(@AuthenticationPrincipal UserDetailsImpl userDetails){
        User client = userDetails.getUser();
        List<SalesOrderDTO> orders = salesOrderService.getMyOrder(client);
        return ResponseEntity.ok(orders);
    }

    @GetMapping("/orders/{orderId}")
    @PreAuthorize("hasAuthority('CLIENT')")
    public ResponseEntity<SalesOrderDTO> getOrderById(@PathVariable Long orderId , @AuthenticationPrincipal UserDetailsImpl userDetails){
        User client = userDetails.getUser();
        SalesOrderDTO order = salesOrderService.getOrder(orderId,client);
        return ResponseEntity.ok(order);
    }
}
