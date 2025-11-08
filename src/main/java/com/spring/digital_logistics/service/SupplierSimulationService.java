package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.request.inventory.MovementRequestDTO;
import com.spring.digital_logistics.dto.response.supplier.SupplierDTO;
import com.spring.digital_logistics.entity.PurchaseOrder;
import com.spring.digital_logistics.entity.PurchaseOrderLine;
import com.spring.digital_logistics.entity.enums.PurchaseOrderStatus;
import com.spring.digital_logistics.repository.PurchaseOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SupplierSimulationService {

    private static final Logger log = LoggerFactory.getLogger(SupplierSimulationService.class);
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final InventoryService inventoryService;

    public SupplierSimulationService(PurchaseOrderRepository purchaseOrderRepository, InventoryService inventoryService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.inventoryService = inventoryService;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void simulateSupplierDeliveries(){
        log.info("--- [SIMULATION] Recherche des commandes envoyées à simuler comme reçues ---");

        List<PurchaseOrder> sentOrders = purchaseOrderRepository.findByStatus(PurchaseOrderStatus.SENT);

        if (sentOrders.isEmpty()){
            log.info("--- [SIMULATION] Aucune commande envoyée à traiter. ---");
            return;
        }

        for (PurchaseOrder order : sentOrders){
            log.info("--- [SIMULATION] La commande #{} a été livrée par le fournisseur. Passage au statut RECEIVED. ---", order.getId());

            for (PurchaseOrderLine line : order.getLines()){
                MovementRequestDTO inboundMovement = new MovementRequestDTO();
                inboundMovement.setProductId(line.getProduct().getId());
                inboundMovement.setWarehouseId(line.getPurchaseOrder().getDestinationWarehouse().getId());
                inboundMovement.setQuantity(line.getQuantity());

                inventoryService.recordInboundMovement(inboundMovement);

                log.info("    -> [INVENTORY] +{} unités du produit SKU {} ajoutées à l'entrepôt {}.", line.getQuantity() , line.getProduct().getSku() , order.getDestinationWarehouse().getCode());
            }

            order.setStatus(PurchaseOrderStatus.RECEIVED);
            purchaseOrderRepository.save(order);
        }

        log.info("--- [SIMULATION] {} commandes ont été marquées comme reçues. ---" , sentOrders.size());
    }
}
