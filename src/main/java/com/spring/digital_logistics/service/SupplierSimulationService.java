package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.response.supplier.SupplierDTO;
import com.spring.digital_logistics.entity.PurchaseOrder;
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

    public SupplierSimulationService(PurchaseOrderRepository purchaseOrderRepository) {
        this.purchaseOrderRepository = purchaseOrderRepository;
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
            order.setStatus(PurchaseOrderStatus.RECEIVED);
            purchaseOrderRepository.save(order);
        }

        log.info("--- [SIMULATION] {} commandes ont été marquées comme reçues. ---" , sentOrders.size());
    }
}
