package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.request.adjustement.AdjustmentRequestDTO;
import com.spring.digital_logistics.dto.request.inventory.MovementRequestDTO;
import com.spring.digital_logistics.dto.response.inventory.InventoryDTO;
import com.spring.digital_logistics.entity.*;
import com.spring.digital_logistics.entity.enums.MovementType;
import com.spring.digital_logistics.entity.enums.SalesOrderLineStatus;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.exception.StockUnavailableException;
import com.spring.digital_logistics.mapper.InventoryMapper;
import com.spring.digital_logistics.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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

    public List<InventoryDTO> getAllInventories(){
        return inventoryRepository.findAll().stream().map(inventoryMapper::toDto).toList();
    }

    public List<InventoryDTO> getInventoriesByWarehouse(Long id){
        List<Inventory> inventory = inventoryRepository.findByWarehouseId(id)
                .orElseThrow(() -> new ResourceNotFoundException("inventory non trouvé avec l'ID de warehouse suivant : " + id));
        return inventory.stream().map(inventoryMapper::toDto).toList();
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
    public boolean reserveStockForOrder(SalesOrder order){
        log.info("Tentative de réservation de stock pour la commande #{}", order.getId());
        boolean allLineFullyReserved = true;

        for (SalesOrderLine line : order.getLines()){
            Product product = line.getProduct();
            Warehouse warehouse = line.getSalesOrder().getWarehouse();
            int quantityToReserve = line.getQuantity();

            Inventory inventory = inventoryRepository.findByProductAndWarehouse(product,warehouse)
                    .orElse(new Inventory(product,warehouse,0,0));

            int availableStock = inventory.getQtyOnHand() - inventory.getQtyReserved();

            if (availableStock >= quantityToReserve){
                log.info("   -> [OK] Stock suffisant pour SKU {}. Demandé: {}, Disponible: {}",
                        product.getSku(), quantityToReserve, availableStock);

                inventory.setQtyReserved(inventory.getQtyReserved() + quantityToReserve);
                inventoryRepository.save(inventory);


                line.setStatus(SalesOrderLineStatus.RESERVED);

            } else {
                log.warn("   -> [!!] Stock INSUFFISANT pour SKU {}. Demandé: {}, Disponible: {}. Passage en backorder.",
                        product.getSku(), quantityToReserve, availableStock);

                allLineFullyReserved = false;
                line.setStatus(SalesOrderLineStatus.BACKORDERED);
            }
        }

        if(allLineFullyReserved) {
            log.info("Réservation terminée pour la commande #{}. Toutes les lignes sont réservées.", order.getId());
        } else {
            log.warn("Réservation terminée pour la commande #{}. Une ou plusieurs lignes sont en backorder.", order.getId());
        }

        return allLineFullyReserved;
    }

    @Transactional
    public void recordOutboundMovementForOrder(SalesOrder order){
        log.info("Enregistrement du mouvement OUTBOUND pour la commande #{}", order.getId());

        for (SalesOrderLine line : order.getLines()){
            Product product = line.getProduct();
            Warehouse warehouse = line.getSalesOrder().getWarehouse();
            int quantityToShip = line.getQuantity();

            Inventory inventory = inventoryRepository.findByProductAndWarehouse(product, warehouse)
                    .orElseThrow(() -> new IllegalStateException(String.format("Erreur critique: Inventaire introuvable pour SKU %s lors de l'expédition.", product.getSku())));

            if (inventory.getQtyOnHand() < quantityToShip || inventory.getQtyReserved() < quantityToShip) {
                throw new IllegalStateException(String.format("Incohérence de stock pour SKU %s. Stock < Quantité expédiée.", product.getSku()));
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

            log.info("   -> [OUTBOUND] {} unités du SKU {} sorties de {}. Stock final: {} | Réservé final: {}", quantityToShip, product.getSku(), warehouse.getCode(), inventory.getQtyOnHand(), inventory.getQtyReserved());
        }

    }

    public void shipStock(Product product,Warehouse sourceWarehouse,int quantity){
        Inventory inventorySource = inventoryRepository.findByProductAndWarehouse(product,sourceWarehouse).orElseThrow(() -> new ResourceNotFoundException("inventorie non trouvé"));

        int newQuantityOnHand =  inventorySource.getQtyOnHand() - quantity;
        inventorySource.setQtyOnHand(newQuantityOnHand);

        inventoryRepository.save(inventorySource);

    }

    public void receiveStock(Product product,Warehouse destinationWarehouse,int quantity){
        Inventory inventorydestination = inventoryRepository.findByProductAndWarehouse(product,destinationWarehouse).orElseThrow(() -> new ResourceNotFoundException("inventorie non trouvé"));

        int newQuantityOnHand =  inventorydestination.getQtyOnHand() + quantity;
        inventorydestination.setQtyOnHand(newQuantityOnHand);

        Inventory savedQuantity = inventoryRepository.save(inventorydestination);

    }
}
