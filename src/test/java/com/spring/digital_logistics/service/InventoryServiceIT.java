package com.spring.digital_logistics.service;

import com.spring.digital_logistics.IntegrationTestBase;
import com.spring.digital_logistics.entity.*;
import com.spring.digital_logistics.entity.enums.SalesOrderLineStatus;
import com.spring.digital_logistics.entity.enums.SalesOrderStatus;
import com.spring.digital_logistics.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
public class InventoryServiceIT extends IntegrationTestBase {
    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void reserveStockForOrder_whenStockIsSufficient_shouldUpdateDatabaseCorrectly() {
        User client = new User();
        client.setEmail("testclient@example.com");
        client.setPassword("password");
        client.setFirstName("Test");
        client.setLastName("Client");
        User savedClient = userRepository.save(client);

        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-INT-TEST", "Integration Test Warehouse"));
        Product product = productRepository.save(new Product("INT-TSHIRT", "T-Shirt pour Test d'Intégration","null", new BigDecimal(25),true));

        inventoryRepository.save(new Inventory(product, warehouse, 10, 0));

        SalesOrder order = new SalesOrder();
        order.setWarehouse(warehouse);
        order.setClient(savedClient);
        order.setStatus(SalesOrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());
        SalesOrderLine line = new SalesOrderLine();
        line.setProduct(product);
        line.setQuantity(5);
        line.setStatus(SalesOrderLineStatus.CREATED);
        order.addLine(line);
        salesOrderRepository.save(order);


        boolean result = inventoryService.reserveStockForOrder(order);


        assertTrue(result);

        Inventory updatedInventory = inventoryRepository.findByProductAndWarehouse(product, warehouse).orElseThrow();
        SalesOrder updatedOrder = salesOrderRepository.findById(order.getId()).orElseThrow();

        assertEquals(5, updatedInventory.getQtyReserved(), "La quantité réservée dans la BDD devrait être 5.");

        assertEquals(SalesOrderLineStatus.RESERVED, updatedOrder.getLines().get(0).getStatus(), "Le statut de la ligne devrait être RESERVED.");
    }

}
