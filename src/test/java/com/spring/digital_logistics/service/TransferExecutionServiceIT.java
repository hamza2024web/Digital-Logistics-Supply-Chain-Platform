package com.spring.digital_logistics.service;

import com.spring.digital_logistics.IntegrationTestBase;
import com.spring.digital_logistics.entity.Inventory;
import com.spring.digital_logistics.entity.Product;
import com.spring.digital_logistics.entity.TransferOrder;
import com.spring.digital_logistics.entity.Warehouse;
import com.spring.digital_logistics.entity.enums.TransferStatus;
import com.spring.digital_logistics.repository.InventoryRepository;
import com.spring.digital_logistics.repository.ProductRepository;
import com.spring.digital_logistics.repository.TransferOrderRepository;
import com.spring.digital_logistics.repository.WarehouseRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class TransferExecutionServiceIT extends IntegrationTestBase {

    @Autowired
    private TransferExecutionService transferExecutionService;

    @Autowired
    private TransferOrderRepository transferOrderRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    // ========== TESTS executePendingTransfers ==========

    @Test
    void executePendingTransfers_withPendingOrder_shouldUpdateInventory() {
        Warehouse source = warehouseRepository.save(new Warehouse("W-SOURCE", "Source Warehouse"));
        Warehouse destination = warehouseRepository.save(new Warehouse("W-DEST", "Destination Warehouse"));
        Product product = productRepository.save(
                new Product("SKU-TRANSFER", "Product Transfer", "Description", new BigDecimal("10.00"), true)
        );

        inventoryRepository.save(new Inventory(product, source, 50, 0));
        inventoryRepository.save(new Inventory(product, destination, 10, 0));

        TransferOrder transfer = new TransferOrder();
        transfer.setProduct(product);
        transfer.setSourceWarehouse(source);
        transfer.setDestinationWarehouse(destination);
        transfer.setQuantity(20);
        transfer.setStatus(TransferStatus.PENDING);
        transfer.setCreationDate(LocalDateTime.now());
        transferOrderRepository.save(transfer);

        transferExecutionService.executePendingTransfers();

        TransferOrder updatedTransfer = transferOrderRepository.findById(transfer.getId()).orElseThrow();
        assertEquals(TransferStatus.COMPLETED, updatedTransfer.getStatus());

        Inventory sourceInventory = inventoryRepository.findByProductAndWarehouse(product, source).orElseThrow();
        assertEquals(30, sourceInventory.getQtyOnHand());

        Inventory destInventory = inventoryRepository.findByProductAndWarehouse(product, destination).orElseThrow();
        assertEquals(30, destInventory.getQtyOnHand());
    }

    @Test
    void executePendingTransfers_withMultiplePendingOrders_shouldExecuteAll() {
        Warehouse source = warehouseRepository.save(new Warehouse("W-SRC", "Source"));
        Warehouse dest = warehouseRepository.save(new Warehouse("W-DST", "Destination"));
        Product product1 = productRepository.save(
                new Product("SKU-001", "Product 1", "Description", new BigDecimal("10.00"), true)
        );
        Product product2 = productRepository.save(
                new Product("SKU-002", "Product 2", "Description", new BigDecimal("20.00"), true)
        );

        inventoryRepository.save(new Inventory(product1, source, 100, 0));
        inventoryRepository.save(new Inventory(product1, dest, 0, 0));
        inventoryRepository.save(new Inventory(product2, source, 100, 0));
        inventoryRepository.save(new Inventory(product2, dest, 0, 0));

        TransferOrder transfer1 = createTransferOrder(product1, source, dest, 15);
        TransferOrder transfer2 = createTransferOrder(product2, source, dest, 25);
        transferOrderRepository.save(transfer1);
        transferOrderRepository.save(transfer2);

        transferExecutionService.executePendingTransfers();

        TransferOrder updated1 = transferOrderRepository.findById(transfer1.getId()).orElseThrow();
        TransferOrder updated2 = transferOrderRepository.findById(transfer2.getId()).orElseThrow();
        assertEquals(TransferStatus.COMPLETED, updated1.getStatus());
        assertEquals(TransferStatus.COMPLETED, updated2.getStatus());

        Inventory inv1Source = inventoryRepository.findByProductAndWarehouse(product1, source).orElseThrow();
        assertEquals(85, inv1Source.getQtyOnHand());

        Inventory inv2Source = inventoryRepository.findByProductAndWarehouse(product2, source).orElseThrow();
        assertEquals(75, inv2Source.getQtyOnHand());
    }

    @Test
    void executePendingTransfers_withNoePendingOrders_shouldDoNothing() {
        Warehouse source = warehouseRepository.save(new Warehouse("W-NO-PENDING", "Source"));
        Warehouse dest = warehouseRepository.save(new Warehouse("W-NO-DEST", "Destination"));
        Product product = productRepository.save(
                new Product("SKU-NO-PENDING", "Product", "Description", new BigDecimal("10.00"), true)
        );

        inventoryRepository.save(new Inventory(product, source, 50, 0));
        inventoryRepository.save(new Inventory(product, dest, 10, 0));

        TransferOrder completedTransfer = createTransferOrder(product, source, dest, 5);
        completedTransfer.setStatus(TransferStatus.COMPLETED);
        transferOrderRepository.save(completedTransfer);

        transferExecutionService.executePendingTransfers();

        Inventory sourceInventory = inventoryRepository.findByProductAndWarehouse(product, source).orElseThrow();
        assertEquals(50, sourceInventory.getQtyOnHand());

        Inventory destInventory = inventoryRepository.findByProductAndWarehouse(product, dest).orElseThrow();
        assertEquals(10, destInventory.getQtyOnHand());
    }

    @Test
    void executePendingTransfers_shouldOnlyProcessPendingStatus() {
        Warehouse source = warehouseRepository.save(new Warehouse("W-STATUS", "Source"));
        Warehouse dest = warehouseRepository.save(new Warehouse("W-STATUS-DEST", "Destination"));
        Product product = productRepository.save(
                new Product("SKU-STATUS", "Product", "Description", new BigDecimal("10.00"), true)
        );

        inventoryRepository.save(new Inventory(product, source, 100, 0));
        inventoryRepository.save(new Inventory(product, dest, 0, 0));

        TransferOrder pendingTransfer = createTransferOrder(product, source, dest, 10);
        pendingTransfer.setStatus(TransferStatus.PENDING);

        TransferOrder completedTransfer = createTransferOrder(product, source, dest, 10);
        completedTransfer.setStatus(TransferStatus.COMPLETED);

        transferOrderRepository.save(pendingTransfer);
        transferOrderRepository.save(completedTransfer);

        transferExecutionService.executePendingTransfers();

        TransferOrder updatedPending = transferOrderRepository.findById(pendingTransfer.getId()).orElseThrow();
        TransferOrder updatedCompleted = transferOrderRepository.findById(completedTransfer.getId()).orElseThrow();

        assertEquals(TransferStatus.COMPLETED, updatedPending.getStatus());
        assertEquals(TransferStatus.COMPLETED, updatedCompleted.getStatus());

        Inventory sourceInventory = inventoryRepository.findByProductAndWarehouse(product, source).orElseThrow();
        assertEquals(90, sourceInventory.getQtyOnHand());
    }

    // ========== Helper Methods ==========

    private TransferOrder createTransferOrder(Product product, Warehouse source, Warehouse dest, int quantity) {
        TransferOrder transfer = new TransferOrder();
        transfer.setProduct(product);
        transfer.setSourceWarehouse(source);
        transfer.setDestinationWarehouse(dest);
        transfer.setQuantity(quantity);
        transfer.setStatus(TransferStatus.PENDING);
        transfer.setCreationDate(LocalDateTime.now());
        return transfer;
    }
}