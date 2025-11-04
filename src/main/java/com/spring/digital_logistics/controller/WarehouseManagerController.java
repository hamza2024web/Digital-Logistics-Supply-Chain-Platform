package com.spring.digital_logistics.controller;

import com.spring.digital_logistics.dto.request.adjustement.AdjustmentRequestDTO;
import com.spring.digital_logistics.dto.request.inventory.MovementRequestDTO;
import com.spring.digital_logistics.dto.response.inventory.InventoryDTO;
import com.spring.digital_logistics.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouse-manager")
public class WarehouseManagerController {

    private final InventoryService inventoryService;

    public WarehouseManagerController(InventoryService inventoryService){
        this.inventoryService = inventoryService;
    }

    @GetMapping("/hello")
    @PreAuthorize("hasAuthority('WAREHOUSE_MANAGER')")
    public ResponseEntity<String> sayHelloWarehouseManager(){
        return ResponseEntity.ok("Bonjour, Warehouse Manager ! Si vous voyez ce message, c'est que votre rôle est bien reconnu.");
    }

    @PostMapping("/inventory/inbound")
    @PreAuthorize("hasAuthority('WAREHOUSE_MANAGER')")
    public ResponseEntity<InventoryDTO> recordInbound(@Valid @RequestBody MovementRequestDTO movementRequest){
        InventoryDTO updatedInventory = inventoryService.recordInboundMovement(movementRequest);
        return ResponseEntity.ok(updatedInventory);
    }

    @PostMapping("/inventory/outbound")
    @PreAuthorize("hasAuthority('WAREHOUSE_MANAGER')")
    public ResponseEntity<InventoryDTO> recordOutbound(@Valid @RequestBody MovementRequestDTO movementRequest){
        InventoryDTO updatedInventory = inventoryService.recordOutBoundMovement(movementRequest);
        return ResponseEntity.ok(updatedInventory);
    }

    @PostMapping("/inventory/adjustment")
    @PreAuthorize("hasAuthority('WAREHOUSE_MANAGER')")
    public ResponseEntity<InventoryDTO> recordAdjustment(@Valid @RequestBody AdjustmentRequestDTO adjustmentRequest){
        InventoryDTO updatedInventory = inventoryService.recordAdjustement(adjustmentRequest);
        return ResponseEntity.ok(updatedInventory);
    }
}
