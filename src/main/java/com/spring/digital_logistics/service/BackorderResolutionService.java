package com.spring.digital_logistics.service;

import com.spring.digital_logistics.repository.InventoryRepository;
import com.spring.digital_logistics.repository.SalesOrderLineRepository;
import com.spring.digital_logistics.repository.TransferOrderRepository;
import com.spring.digital_logistics.repository.WarehouseRepository;
import org.springframework.stereotype.Service;

@Service
public class BackorderResolutionService {

    private final SalesOrderLineRepository salesOrderLineRepository;
    private final InventoryRepository inventoryRepository;
    private final WarehouseRepository warehouseRepository;
    private final TransferOrderRepository transferOrderRepository;

    public BackorderResolutionService(SalesOrderLineRepository salesOrderLineRepository, InventoryRepository inventoryRepository, WarehouseRepository warehouseRepository, TransferOrderRepository transferOrderRepository) {
        this.salesOrderLineRepository = salesOrderLineRepository;
        this.inventoryRepository = inventoryRepository;
        this.warehouseRepository = warehouseRepository;
        this.transferOrderRepository = transferOrderRepository;
    }


}
