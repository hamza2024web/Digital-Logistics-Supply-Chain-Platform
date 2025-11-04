package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.request.inventory.MovementRequestDTO;
import com.spring.digital_logistics.dto.response.inventory.InventoryDTO;
import com.spring.digital_logistics.entity.Inventory;
import com.spring.digital_logistics.entity.InventoryMovement;
import com.spring.digital_logistics.entity.Product;
import com.spring.digital_logistics.entity.Warehouse;
import com.spring.digital_logistics.entity.enums.MovementType;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.mapper.InventoryMapper;
import com.spring.digital_logistics.repository.InventoryMovementRepository;
import com.spring.digital_logistics.repository.InventoryRepository;
import com.spring.digital_logistics.repository.ProductRepository;
import com.spring.digital_logistics.repository.WarehouseRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InventoryService {

    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final InventoryRepository inventoryRepository;
    private final InventoryMovementRepository inventoryMovementRepository;
    private final InventoryMapper inventoryMapper;

    public InventoryService(ProductRepository productRepository, WarehouseRepository warehouseRepository, InventoryRepository inventoryRepository, InventoryMovementRepository inventoryMovementRepository, InventoryMapper inventoryMapper) {
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.inventoryRepository = inventoryRepository;
        this.inventoryMovementRepository = inventoryMovementRepository;
        this.inventoryMapper = inventoryMapper;
    }

    @Transactional
    public InventoryDTO recordInboundMovement(MovementRequestDTO movementRequest){

        Product product = productRepository.findById(movementRequest.getProductId()).orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec L'ID : " + movementRequest.getProductId()));

        Warehouse warehouse = warehouseRepository.findById(movementRequest.getWarehouseId()).orElseThrow(() -> new ResourceNotFoundException("Entrepôt non trouvé avec l'ID: " + movementRequest.getWarehouseId()));

        Inventory inventory = inventoryRepository.findByProductAndWarehouse(product, warehouse)
                .orElseGet(() -> {
                    Inventory newInventory = new Inventory();
                    newInventory.setProduct(product);
                    newInventory.setWarehouse(warehouse);
                    newInventory.setQtyOnHand(0);
                    newInventory.setQtyReserved(0);
                    return newInventory;
                });

        inventory.setQtyOnHand(inventory.getQtyOnHand() + movementRequest.getQuantity());
        Inventory savedInventory = inventoryRepository.save(inventory);

        InventoryMovement movement = new InventoryMovement();
        movement.setProduct(product);
        movement.setWarehouse(warehouse);
        movement.setType(MovementType.INBOUND);
        movement.setQty(movementRequest.getQuantity());
        movement.setOccurredAt(LocalDateTime.now());
        inventoryMovementRepository.save(movement);

        return inventoryMapper.toDto(savedInventory);
    }
}
