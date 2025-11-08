package com.spring.digital_logistics.controller;

import com.spring.digital_logistics.dto.request.salesOrder.SalesOrderRequestDTO;
import com.spring.digital_logistics.dto.response.salesOrder.SalesOrderDTO;
import com.spring.digital_logistics.service.SalesOrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/client")
public class ClientController {

    private final SalesOrderService salesOrderService;

    public ClientController(SalesOrderService salesOrderService){
        this.salesOrderService = salesOrderService;
    }

//    public ResponseEntity<SalesOrderDTO> createSalesOrder(@Valid @RequestBody SalesOrderRequestDTO createDTO){
//        SalesOrderDTO salesOrder = salesOrderService.createSalesOrder(createDTO);
//        return new ResponseEntity<>(salesOrder , HttpStatus.CREATED);
//    }
}
