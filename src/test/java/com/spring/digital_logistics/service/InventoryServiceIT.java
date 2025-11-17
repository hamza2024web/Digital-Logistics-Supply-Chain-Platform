package com.spring.digital_logistics.service;

import com.spring.digital_logistics.IntegrationTestBase;
import com.spring.digital_logistics.dto.request.adjustement.AdjustmentRequestDTO;
import com.spring.digital_logistics.dto.request.inventory.MovementRequestDTO;
import com.spring.digital_logistics.dto.response.inventory.InventoryDTO;
import com.spring.digital_logistics.entity.*;
import com.spring.digital_logistics.entity.enums.MovementType;
import com.spring.digital_logistics.entity.enums.SalesOrderLineStatus;
import com.spring.digital_logistics.entity.enums.SalesOrderStatus;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.exception.StockUnavailableException;
import com.spring.digital_logistics.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

    @Autowired
    private InventoryMovementRepository inventoryMovementRepository;

    // ========== TESTS recordInboundMovement ==========

    @Test
    void recordInboundMovement_whenInventoryExists_shouldUpdateDatabase() {
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-IN-001", "Warehouse Inbound"));
        Product product = productRepository.save(new Product("SKU-IN-001", "Product Inbound", "Description", new BigDecimal(20), true));
        inventoryRepository.save(new Inventory(product, warehouse, 10, 0));

        MovementRequestDTO request = new MovementRequestDTO();
        request.setProductId(product.getId());
        request.setWarehouseId(warehouse.getId());
        request.setQuantity(5);

        InventoryDTO result = inventoryService.recordInboundMovement(request);

        assertNotNull(result);
        Inventory updatedInventory = inventoryRepository.findByProductAndWarehouse(product, warehouse).orElseThrow();
        assertEquals(15, updatedInventory.getQtyOnHand());

        List<InventoryMovement> movements = inventoryMovementRepository.findAll();
        assertTrue(movements.stream().anyMatch(m ->
                m.getType() == MovementType.INBOUND && m.getQty() == 5
        ));
    }

    @Test
    void recordInboundMovement_whenInventoryDoesNotExist_shouldCreateNew() {
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-IN-002", "Warehouse Inbound New"));
        Product product = productRepository.save(new Product("SKU-IN-002", "Product New", "Description", new BigDecimal(25), true));

        MovementRequestDTO request = new MovementRequestDTO();
        request.setProductId(product.getId());
        request.setWarehouseId(warehouse.getId());
        request.setQuantity(10);

        InventoryDTO result = inventoryService.recordInboundMovement(request);

        assertNotNull(result);
        Inventory createdInventory = inventoryRepository.findByProductAndWarehouse(product, warehouse).orElseThrow();
        assertEquals(10, createdInventory.getQtyOnHand());
        assertEquals(0, createdInventory.getQtyReserved());
    }

    @Test
    void recordInboundMovement_whenProductNotFound_shouldThrowException() {
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-IN-003", "Warehouse"));

        MovementRequestDTO request = new MovementRequestDTO();
        request.setProductId(9999L);
        request.setWarehouseId(warehouse.getId());
        request.setQuantity(5);

        assertThrows(ResourceNotFoundException.class,
                () -> inventoryService.recordInboundMovement(request));
    }

    // ========== TESTS recordOutBoundMovement ==========

    @Test
    void recordOutBoundMovement_whenStockSufficient_shouldUpdateDatabase() {
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-OUT-001", "Warehouse Outbound"));
        Product product = productRepository.save(new Product("SKU-OUT-001", "Product Outbound", "Description", new BigDecimal(30), true));
        inventoryRepository.save(new Inventory(product, warehouse, 20, 0));

        MovementRequestDTO request = new MovementRequestDTO();
        request.setProductId(product.getId());
        request.setWarehouseId(warehouse.getId());
        request.setQuantity(8);

        InventoryDTO result = inventoryService.recordOutBoundMovement(request);

        assertNotNull(result);
        Inventory updatedInventory = inventoryRepository.findByProductAndWarehouse(product, warehouse).orElseThrow();
        assertEquals(12, updatedInventory.getQtyOnHand());
    }

    @Test
    void recordOutBoundMovement_whenStockInsufficient_shouldThrowException() {
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-OUT-002", "Warehouse"));
        Product product = productRepository.save(new Product("SKU-OUT-002", "Product", "Description", new BigDecimal(30), true));
        inventoryRepository.save(new Inventory(product, warehouse, 5, 0));

        MovementRequestDTO request = new MovementRequestDTO();
        request.setProductId(product.getId());
        request.setWarehouseId(warehouse.getId());
        request.setQuantity(10);

        assertThrows(StockUnavailableException.class,
                () -> inventoryService.recordOutBoundMovement(request));
    }

    @Test
    void recordOutBoundMovement_whenInventoryNotFound_shouldThrowException() {
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-OUT-003", "Warehouse"));
        Product product = productRepository.save(new Product("SKU-OUT-003", "Product", "Description", new BigDecimal(30), true));

        MovementRequestDTO request = new MovementRequestDTO();
        request.setProductId(product.getId());
        request.setWarehouseId(warehouse.getId());
        request.setQuantity(5);

        assertThrows(StockUnavailableException.class,
                () -> inventoryService.recordOutBoundMovement(request));
    }

    // ========== TESTS recordAdjustement ==========

    @Test
    void recordAdjustement_withPositiveValue_shouldIncreaseStock() {
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-ADJ-001", "Warehouse Adjustment"));
        Product product = productRepository.save(new Product("SKU-ADJ-001", "Product Adj", "Description", new BigDecimal(15), true));
        inventoryRepository.save(new Inventory(product, warehouse, 10, 0));

        AdjustmentRequestDTO request = new AdjustmentRequestDTO();
        request.setProductId(product.getId());
        request.setWarehouseId(warehouse.getId());
        request.setQuantity(7);

        InventoryDTO result = inventoryService.recordAdjustement(request);

        assertNotNull(result);
        Inventory updatedInventory = inventoryRepository.findByProductAndWarehouse(product, warehouse).orElseThrow();
        assertEquals(17, updatedInventory.getQtyOnHand());
    }

    @Test
    void recordAdjustement_withNegativeValue_shouldDecreaseStock() {
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-ADJ-002", "Warehouse"));
        Product product = productRepository.save(new Product("SKU-ADJ-002", "Product", "Description", new BigDecimal(15), true));
        inventoryRepository.save(new Inventory(product, warehouse, 15, 0));

        AdjustmentRequestDTO request = new AdjustmentRequestDTO();
        request.setProductId(product.getId());
        request.setWarehouseId(warehouse.getId());
        request.setQuantity(-5);

        InventoryDTO result = inventoryService.recordAdjustement(request);

        assertNotNull(result);
        Inventory updatedInventory = inventoryRepository.findByProductAndWarehouse(product, warehouse).orElseThrow();
        assertEquals(10, updatedInventory.getQtyOnHand());
    }

    @Test
    void recordAdjustement_withNegativeExceedingStock_shouldThrowException() {
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-ADJ-003", "Warehouse"));
        Product product = productRepository.save(new Product("SKU-ADJ-003", "Product", "Description", new BigDecimal(15), true));
        inventoryRepository.save(new Inventory(product, warehouse, 5, 0));

        AdjustmentRequestDTO request = new AdjustmentRequestDTO();
        request.setProductId(product.getId());
        request.setWarehouseId(warehouse.getId());
        request.setQuantity(-10);

        assertThrows(StockUnavailableException.class,
                () -> inventoryService.recordAdjustement(request));
    }

    // ========== TESTS reserveStockForOrder ==========

    @Test
    void reserveStockForOrder_whenStockSufficient_shouldUpdateDatabase() {
        User client = userRepository.save(createTestUser("reserve@test.com"));
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-RES-001", "Warehouse Reserve"));
        Product product = productRepository.save(new Product("SKU-RES-001", "Product Reserve", "Description", new BigDecimal(25), true));
        inventoryRepository.save(new Inventory(product, warehouse, 20, 0));

        SalesOrder order = new SalesOrder();
        order.setWarehouse(warehouse);
        order.setClient(client);
        order.setStatus(SalesOrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());

        SalesOrderLine line = new SalesOrderLine();
        line.setProduct(product);
        line.setQuantity(10);
        line.setStatus(SalesOrderLineStatus.CREATED);
        order.addLine(line);

        salesOrderRepository.save(order);

        boolean result = inventoryService.reserveStockForOrder(order);

        assertTrue(result);
        Inventory updatedInventory = inventoryRepository.findByProductAndWarehouse(product, warehouse).orElseThrow();
        assertEquals(10, updatedInventory.getQtyReserved());
        assertEquals(20, updatedInventory.getQtyOnHand());

        SalesOrder updatedOrder = salesOrderRepository.findById(order.getId()).orElseThrow();
        assertEquals(SalesOrderLineStatus.RESERVED, updatedOrder.getLines().get(0).getStatus());
    }

    @Test
    void reserveStockForOrder_whenStockInsufficient_shouldBackorder() {
        User client = userRepository.save(createTestUser("backorder@test.com"));
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-RES-002", "Warehouse"));
        Product product = productRepository.save(new Product("SKU-RES-002", "Product", "Description", new BigDecimal(25), true));
        inventoryRepository.save(new Inventory(product, warehouse, 5, 0));

        SalesOrder order = new SalesOrder();
        order.setWarehouse(warehouse);
        order.setClient(client);
        order.setStatus(SalesOrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());

        SalesOrderLine line = new SalesOrderLine();
        line.setProduct(product);
        line.setQuantity(10);
        line.setStatus(SalesOrderLineStatus.CREATED);
        order.addLine(line);

        salesOrderRepository.save(order);

        boolean result = inventoryService.reserveStockForOrder(order);

        assertFalse(result);
        SalesOrder updatedOrder = salesOrderRepository.findById(order.getId()).orElseThrow();
        assertEquals(SalesOrderLineStatus.BACKORDERED, updatedOrder.getLines().get(0).getStatus());
    }

    // ========== TESTS recordOutboundMovementForOrder ==========

    @Test
    void recordOutboundMovementForOrder_shouldUpdateDatabaseCorrectly() {
        User client = userRepository.save(createTestUser("outbound@test.com"));
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-SHIP-001", "Warehouse Ship"));
        Product product = productRepository.save(new Product("SKU-SHIP-001", "Product Ship", "Description", new BigDecimal(40), true));
        inventoryRepository.save(new Inventory(product, warehouse, 20, 10));

        SalesOrder order = new SalesOrder();
        order.setWarehouse(warehouse);
        order.setClient(client);
        order.setStatus(SalesOrderStatus.RESERVED);
        order.setCreatedAt(LocalDateTime.now());

        SalesOrderLine line = new SalesOrderLine();
        line.setProduct(product);
        line.setQuantity(10);
        line.setStatus(SalesOrderLineStatus.RESERVED);
        order.addLine(line);

        salesOrderRepository.save(order);

        inventoryService.recordOutboundMovementForOrder(order);

        Inventory updatedInventory = inventoryRepository.findByProductAndWarehouse(product, warehouse).orElseThrow();
        assertEquals(10, updatedInventory.getQtyOnHand());
        assertEquals(0, updatedInventory.getQtyReserved());

        List<InventoryMovement> movements = inventoryMovementRepository.findAll();
        assertTrue(movements.stream().anyMatch(m ->
                m.getType() == MovementType.OUTBOUND && m.getQty() == 10
        ));
    }

    // ========== TESTS shipStock & receiveStock ==========

    @Test
    void shipStock_shouldDecreaseSourceStock() {
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-TRANSFER-001", "Source"));
        Product product = productRepository.save(new Product("SKU-TRANSFER-001", "Product", "Description", new BigDecimal(30), true));
        inventoryRepository.save(new Inventory(product, warehouse, 50, 0));

        inventoryService.shipStock(product, warehouse, 15);

        Inventory updatedInventory = inventoryRepository.findByProductAndWarehouse(product, warehouse).orElseThrow();
        assertEquals(35, updatedInventory.getQtyOnHand());
    }

    @Test
    void receiveStock_shouldIncreaseDestinationStock() {
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-TRANSFER-002", "Destination"));
        Product product = productRepository.save(new Product("SKU-TRANSFER-002", "Product", "Description", new BigDecimal(30), true));
        inventoryRepository.save(new Inventory(product, warehouse, 10, 0));

        inventoryService.receiveStock(product, warehouse, 15);

        Inventory updatedInventory = inventoryRepository.findByProductAndWarehouse(product, warehouse).orElseThrow();
        assertEquals(25, updatedInventory.getQtyOnHand());
    }

    // ========== Helper Methods ==========

    private User createTestUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setActive(true);
        return user;
    }
}