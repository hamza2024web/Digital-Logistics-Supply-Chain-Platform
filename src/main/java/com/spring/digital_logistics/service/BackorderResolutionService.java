package com.spring.digital_logistics.service;

import com.spring.digital_logistics.entity.*;
import com.spring.digital_logistics.entity.enums.SalesOrderLineStatus;
import com.spring.digital_logistics.entity.enums.TransferStatus;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.repository.InventoryRepository;
import com.spring.digital_logistics.repository.SalesOrderLineRepository;
import com.spring.digital_logistics.repository.TransferOrderRepository;
import com.spring.digital_logistics.repository.WarehouseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BackorderResolutionService {

    private final SalesOrderLineRepository salesOrderLineRepository;
    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final TransferOrderRepository transferOrderRepository;
    private final InventoryService inventoryService;
    private static final Logger log = LoggerFactory.getLogger(SupplierSimulationService.class);

    public BackorderResolutionService(SalesOrderLineRepository salesOrderLineRepository, InventoryRepository inventoryRepository, WarehouseRepository warehouseRepository, TransferOrderRepository transferOrderRepository, InventoryService inventoryService) {
        this.salesOrderLineRepository = salesOrderLineRepository;
        this.inventoryRepository = inventoryRepository;
        this.warehouseRepository = warehouseRepository;
        this.transferOrderRepository = transferOrderRepository;
        this.inventoryService = inventoryService;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void attemptToResolveBackorders(int quantityToReserve){
        log.info("Commence la recherche des commandes qu'on une status BACKORDER");

        List<SalesOrderLine> orders = salesOrderLineRepository.findByStatus(SalesOrderLineStatus.BACKORDERED);

        for (SalesOrderLine order : orders){
            Warehouse sourceWarehouse = order.getSalesOrder().getWarehouse();
            Product product = order.getProduct();

            Inventory inventory = inventoryRepository.findByProductAndWarehouse(product,sourceWarehouse).orElseThrow(() -> new ResourceNotFoundException("Inventory non trouvé"));

            int availableStock = inventory.getQtyOnHand() - inventory.getQtyReserved();

            log.info("essayer de vérifier est ce que son warehouse il a la quantité suffisant maintenant .");
            if (quantityToReserve > availableStock){
                log.info("son warehouse n'y a pas la quantité suffisant , essayer de le trouver dans les autres inventories .");
                List<Inventory> allInventory = inventoryRepository.findAll();

                for (Inventory thisInventory : allInventory){
                    if (quantityToReserve <= thisInventory.getQtyOnHand() - thisInventory.getQtyReserved()){
                        log.info("trouver l'inventorie qu'est la quantité suffisant .");
                        Warehouse destinationWarehouse = warehouseRepository.findById(thisInventory.getWarehouse().getId()).orElseThrow(() -> new ResourceNotFoundException("Entrepot not found"));
                        TransferOrder transferOrder = new TransferOrder(product,sourceWarehouse,destinationWarehouse,quantityToReserve, TransferStatus.PENDING, LocalDateTime.now());
                        transferOrderRepository.save(transferOrder);
                        order.setStatus(SalesOrderLineStatus.AWAITING_TRANSFER);
                        salesOrderLineRepository.save(order);
                        log.info("changement de status de order a awaiting_transfer et la création d'objet transfer order ");
                        break;
                    } else {
                        continue;
                    }
                }

            } else {
                inventoryService.reserveStockForOrder(order.getSalesOrder());
                break;
            }
        }
    }
}
