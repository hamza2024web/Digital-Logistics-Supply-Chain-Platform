package com.spring.digital_logistics.controller;

import com.spring.digital_logistics.dto.request.adjustement.AdjustmentRequestDTO;
import com.spring.digital_logistics.dto.request.inventory.MovementRequestDTO;
import com.spring.digital_logistics.dto.response.inventory.InventoryDTO;
import com.spring.digital_logistics.entity.User;
import com.spring.digital_logistics.service.InventoryService;
import com.spring.digital_logistics.service.PurchaseOrderService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/warehouse-manager")
public class WarehouseManagerController {

    private final InventoryService inventoryService;
    private final PurchaseOrderService purchaseOrderService;

    public WarehouseManagerController(InventoryService inventoryService, PurchaseOrderService purchaseOrderService){
        this.inventoryService = inventoryService;
        this.purchaseOrderService = purchaseOrderService;
    }

    @GetMapping("/hello")
    @PreAuthorize("hasAuthority('WAREHOUSE_MANAGER')")
    public ResponseEntity<String> sayHelloWarehouseManager(){
        return ResponseEntity.ok("Bonjour, Warehouse Manager ! Si vous voyez ce message, c'est que votre rôle est bien reconnu.");
    }

    @PostMapping("/inventory/{purchase_id}/inbound")
    @PreAuthorize("hasAuthority('WAREHOUSE_MANAGER')")
    public ResponseEntity<InventoryDTO> recordInbound(@Valid @PathVariable Long purchase_id,@AuthenticationPrincipal User warehouse){
        InventoryDTO updatedInventory = purchaseOrderService.receiveOrder(purchase_id,warehouse);
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
