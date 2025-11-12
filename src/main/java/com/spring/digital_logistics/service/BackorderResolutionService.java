package com.spring.digital_logistics.service;

import com.spring.digital_logistics.entity.*;
import com.spring.digital_logistics.entity.enums.SalesOrderLineStatus;
import com.spring.digital_logistics.entity.enums.SalesOrderStatus;
import com.spring.digital_logistics.entity.enums.TransferStatus;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.repository.*;
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
    private final SalesOrderRepository salesOrderRepository;
    private static final Logger log = LoggerFactory.getLogger(SupplierSimulationService.class);

    public BackorderResolutionService(SalesOrderLineRepository salesOrderLineRepository, InventoryRepository inventoryRepository, WarehouseRepository warehouseRepository, TransferOrderRepository transferOrderRepository, InventoryService inventoryService, SalesOrderRepository salesOrderRepository) {
        this.salesOrderLineRepository = salesOrderLineRepository;
        this.inventoryRepository = inventoryRepository;
        this.warehouseRepository = warehouseRepository;
        this.transferOrderRepository = transferOrderRepository;
        this.inventoryService = inventoryService;
        this.salesOrderRepository = salesOrderRepository;
    }

    @Scheduled(fixedRate = 6000)
    @Transactional
    public void resolveBackordersAndAwaitingTransfers(){
        log.info("--- [AGENT] Démarrage du cycle de résolution ---");
        resolveAwaitingTransferLignes();

        resolveBackorderedLines();
        log.info("--- [AGENT] Fin du cycle de résolution ---");
    }

    private void resolveAwaitingTransferLignes(){
        List<SalesOrderLine> awaitingLines = salesOrderLineRepository.findByStatus(SalesOrderLineStatus.AWAITING_TRANSFER);
        if (awaitingLines.isEmpty()){
            return;
        }

        log.info("[AGENT] {} lignes en 'AWAITING_TRANSFER' trouvées. Tentative de réservation finale...", awaitingLines.size());
        for (SalesOrderLine line : awaitingLines){
            Product product = line.getProduct();
            Warehouse destinationWarehouse = line.getSalesOrder().getWarehouse();
            int quantityNeeded = line.getQuantity();

            Inventory localInventory = inventoryRepository.findByProductAndWarehouse(product,destinationWarehouse).orElse(new Inventory(product,destinationWarehouse,0,0));
            int availableStock = localInventory.getQtyOnHand() - localInventory.getQtyReserved();

            if (availableStock >= quantityNeeded){
                log.info("   -> [SUCCÈS] Le stock pour SKU {} est maintenant disponible à {}. Réservation finale.", product.getSku(), destinationWarehouse.getCode());

                localInventory.setQtyReserved(localInventory.getQtyReserved() + quantityNeeded);
                inventoryRepository.save(localInventory);

                line.setStatus(SalesOrderLineStatus.RESERVED);
                salesOrderLineRepository.save(line);

                checkAndReserveParentOrder(line.getSalesOrder());
            }
        }
    }

    private void resolveBackorderedLines() {
        List<SalesOrderLine> backorderedLines = salesOrderLineRepository.findByStatus(SalesOrderLineStatus.BACKORDERED);
        if (backorderedLines.isEmpty()) {
            return;
        }

        log.info("[AGENT] {} lignes en 'BACKORDERED' trouvées. Recherche de solutions de transfert...", backorderedLines.size());
        for (SalesOrderLine line : backorderedLines) {
            Product productToFind = line.getProduct();
            Warehouse destinationWarehouse = line.getSalesOrder().getWarehouse();
            int quantityNeeded = line.getQuantity();

            List<Warehouse> otherWarehouses = warehouseRepository.findAll().stream()
                    .filter(w -> !w.getId().equals(destinationWarehouse.getId()))
                    .toList();

            for (Warehouse sourceWarehouse : otherWarehouses) {
                Inventory remoteInventory = inventoryRepository.findByProductAndWarehouse(productToFind, sourceWarehouse).orElse(new Inventory());
                int remoteAvailableStock = remoteInventory.getQtyOnHand() - remoteInventory.getQtyReserved();

                if (remoteAvailableStock >= quantityNeeded) {
                    log.info("   -> [SOLUTION] Stock trouvé pour SKU {} à {}. Création d'un ordre de transfert.", productToFind.getSku(), sourceWarehouse.getCode());

                    TransferOrder transferOrder = new TransferOrder(productToFind, sourceWarehouse, destinationWarehouse, quantityNeeded, TransferStatus.PENDING, LocalDateTime.now());
                    transferOrderRepository.save(transferOrder);

                    line.setStatus(SalesOrderLineStatus.AWAITING_TRANSFER);
                    salesOrderLineRepository.save(line);

                    break;
                }
            }
        }
    }

    private void checkAndReserveParentOrder(SalesOrder salesOrder){
        SalesOrder freshOrder = salesOrderRepository.findById(salesOrder.getId()).orElseThrow(() -> new IllegalStateException("Erreur critique: la commande parent n'a pas été trouvée."));

        boolean allLinesAreReserved = freshOrder.getLines().stream()
                .allMatch(line -> line.getStatus() == SalesOrderLineStatus.RESERVED);

        if (allLinesAreReserved){
            log.info("   -> [COMMANDE PRÊTE] Toutes les lignes de la commande #{} sont maintenant réservées. Mise à jour du statut global.", freshOrder.getId());

            freshOrder.setStatus(SalesOrderStatus.RESERVED);
            salesOrderRepository.save(freshOrder);
        }
    }
}
