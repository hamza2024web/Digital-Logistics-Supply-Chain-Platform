package com.spring.digital_logistics.service;

import com.spring.digital_logistics.entity.TransferOrder;
import com.spring.digital_logistics.entity.enums.TransferStatus;
import com.spring.digital_logistics.repository.TransferOrderRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransferExecutionService {
    private final InventoryService inventoryService;
    private final TransferOrderRepository transferOrderRepository;

    public TransferExecutionService(InventoryService inventoryService, TransferOrderRepository transferOrderRepository) {
        this.inventoryService = inventoryService;
        this.transferOrderRepository = transferOrderRepository;
    }

    @Scheduled(fixedRate = 7000)
    public void executePendingTransfers(){
        List<TransferOrder> transferOrders = transferOrderRepository.findByStatus(TransferStatus.PENDING);

        for (TransferOrder order : transferOrders){
            inventoryService.shipStock(order.getProduct(),order.getSourceWarehouse(),order.getQuantity());
            inventoryService.receiveStock(order.getProduct(),order.getDestinationWarehouse(),order.getQuantity());
            order.setStatus(TransferStatus.COMPLETED);
            transferOrderRepository.save(order);
        }

    }
}
