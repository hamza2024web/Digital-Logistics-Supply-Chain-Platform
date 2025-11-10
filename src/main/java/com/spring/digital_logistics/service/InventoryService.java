package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.request.adjustement.AdjustmentRequestDTO;
import com.spring.digital_logistics.dto.request.inventory.MovementRequestDTO;
import com.spring.digital_logistics.dto.response.inventory.InventoryDTO;
import com.spring.digital_logistics.entity.*;
import com.spring.digital_logistics.entity.enums.MovementType;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.exception.StockUnavailableException;
import com.spring.digital_logistics.mapper.InventoryMapper;
import com.spring.digital_logistics.repository.InventoryMovementRepository;
import com.spring.digital_logistics.repository.InventoryRepository;
import com.spring.digital_logistics.repository.ProductRepository;
import com.spring.digital_logistics.repository.WarehouseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger log = LoggerFactory.getLogger(InventoryService.class);


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

    @Transactional
    public InventoryDTO recordOutBoundMovement(MovementRequestDTO movementRequest){

        Product product = productRepository.findById(movementRequest.getProductId()).orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec L'ID : " + movementRequest.getProductId()));

        Warehouse warehouse = warehouseRepository.findById(movementRequest.getWarehouseId()).orElseThrow(() -> new ResourceNotFoundException("Entrepôt non trouvé avec l'ID: " + movementRequest.getWarehouseId()));

        Inventory inventory = inventoryRepository.findByProductAndWarehouse(product, warehouse).orElseThrow(() -> new StockUnavailableException("Aucun stock trouvé pour ce produit dans cet entrepôt. Impossible de faire une sortie."));

        int quantityToMove = movementRequest.getQuantity();

        int availableStock = inventory.getQtyOnHand() - inventory.getQtyReserved();

        if (availableStock < quantityToMove){
            throw new StockUnavailableException("Stock disponible insuffisant. Demandé : " + quantityToMove + ", Disponible (non réservé) : " + availableStock);
        }

        inventory.setQtyOnHand(inventory.getQtyOnHand() - quantityToMove);
        Inventory savedInventory = inventoryRepository.save(inventory);

        InventoryMovement movement = new InventoryMovement();
        movement.setProduct(product);
        movement.setWarehouse(warehouse);
        movement.setType(MovementType.OUTBOUND);
        movement.setQty(movementRequest.getQuantity());
        movement.setOccurredAt(LocalDateTime.now());
        inventoryMovementRepository.save(movement);

        return inventoryMapper.toDto(savedInventory);
    }

    @Transactional
    public InventoryDTO recordAdjustement(AdjustmentRequestDTO adjustmentRequest){
        Product product = productRepository.findById(adjustmentRequest.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + adjustmentRequest.getProductId()));

        Warehouse warehouse = warehouseRepository.findById(adjustmentRequest.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt non trouvé avec l'ID: " + adjustmentRequest.getWarehouseId()));

        Inventory inventory = inventoryRepository.findByProductAndWarehouse(product,warehouse)
                .orElseGet(() -> {
                    Inventory newInventory = new Inventory();
                    newInventory.setProduct(product);
                    newInventory.setWarehouse(warehouse);
                    newInventory.setQtyOnHand(0);
                    newInventory.setQtyReserved(0);
                    return newInventory;
                });

        int adjustmentQty = adjustmentRequest.getQuantity();

        if (adjustmentQty < 0){
            int availableStock = inventory.getQtyOnHand() - inventory.getQtyReserved();
            if (availableStock < Math.abs(adjustmentQty)) {
                throw new StockUnavailableException("Stock disponible insuffisant pour l'ajustement. Demandé: " + adjustmentQty + ", Disponible (non réservé): " + availableStock);
            }
        }

        inventory.setQtyOnHand(inventory.getQtyOnHand() + adjustmentQty);
        Inventory savedInventory = inventoryRepository.save(inventory);

        InventoryMovement movement = new InventoryMovement();
        movement.setProduct(product);
        movement.setWarehouse(warehouse);
        movement.setType(MovementType.ADJUSTMENT);
        movement.setQty(adjustmentQty);
        movement.setOccurredAt(LocalDateTime.now());
        inventoryMovementRepository.save(movement);

        return inventoryMapper.toDto(savedInventory);
    }

    @Transactional
    public void reserveStockForOrder(SalesOrder order){

        for (SalesOrderLine line : order.getLines()){
            Product product = line.getProduct();
            Warehouse warehouse = line.getSalesOrder().getWarehouse();
            int quantityToReserve = line.getQuantity();

            Inventory inventory = inventoryRepository.findByProductAndWarehouse(product,warehouse)
                    .orElseThrow(() -> new IllegalStateException(String.format(
                            "Aucun inventaire trouvé pour le produit SKU %s dans l'entrepôt %s. Réservation impossible.",
                            product.getSku(), warehouse.getCode())));

            int availableStock = inventory.getQtyOnHand() - inventory.getQtyReserved();

            if (availableStock < quantityToReserve){
                log.warn("Stock insuffisant pour le produit SKU {} ! Disponible: {}, Demandé: {}",product.getSku(),availableStock,quantityToReserve);

                throw new IllegalStateException(String.format("Stock insuffisant pour le produit SKU %s. Quantité disponible: %d, Quantité demandée: %d", product.getSku(), availableStock, quantityToReserve));
            }

            inventory.setQtyReserved(inventory.getQtyReserved() + quantityToReserve);
            inventoryRepository.save(inventory);

            log.info("Stock réservé pour le produit SKU {}: {} unités. Nouveau total réservé: {}",
                    product.getSku(), quantityToReserve, inventory.getQtyReserved());
        }
        log.info("Toutes les lignes de la commande #{} ont été réservées avec succès.", order.getId());
    }

    @Transactional
    public void recordOutboundMovementForOrder(SalesOrder order){
        log.info("Enregistrement du mouvement OUTBOUND pour la commande #{}", order.getId());

        for (SalesOrderLine line : order.getLines()){
            Product product = line.getProduct();
            Warehouse warehouse = line.getSalesOrder().getWarehouse();
            int quantityToShip = line.getQuantity();

            Inventory inventory = inventoryRepository.findByProductAndWarehouse(product, warehouse)
                    .orElseThrow(() -> new IllegalStateException(String.format(
                            "Erreur critique: Inventaire introuvable pour SKU %s lors de l'expédition.", product.getSku())));

            if (inventory.getQtyOnHand() < quantityToShip || inventory.getQtyReserved() < quantityToShip) {
                throw new IllegalStateException(String.format(
                        "Incohérence de stock pour SKU %s. Stock < Quantité expédiée.", product.getSku()));
            }

            inventory.setQtyOnHand(inventory.getQtyOnHand() - quantityToShip);
            inventory.setQtyReserved(inventory.getQtyReserved() - quantityToShip);

            inventoryRepository.save(inventory);

            InventoryMovement movement = new InventoryMovement();
            movement.setProduct(product);
            movement.setWarehouse(warehouse);
            movement.setQty(quantityToShip);
            movement.setType(MovementType.OUTBOUND);
            movement.setOccurredAt(LocalDateTime.now());
            inventoryMovementRepository.save(movement);

            log.info("   -> [OUTBOUND] {} unités du SKU {} sorties de {}. Stock final: {} | Réservé final: {}",
                    quantityToShip, product.getSku(), warehouse.getCode(), inventory.getQtyOnHand(), inventory.getQtyReserved());
        }
    }
}
