package com.spring.digital_logistics.service;

import com.spring.digital_logistics.IntegrationTestBase;
import com.spring.digital_logistics.dto.request.salesOrder.SalesOrderCreateDTO;
import com.spring.digital_logistics.dto.request.salesOrder.SalesOrderLineCreateDTO;
import com.spring.digital_logistics.dto.response.salesOrder.SalesOrderDTO;
import com.spring.digital_logistics.entity.*;
import com.spring.digital_logistics.entity.enums.Role;
import com.spring.digital_logistics.entity.enums.SalesOrderLineStatus;
import com.spring.digital_logistics.entity.enums.SalesOrderStatus;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class SalesOrderServiceIT extends IntegrationTestBase {

    @Autowired
    private SalesOrderService salesOrderService;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    // ========== TESTS createOrder ==========

    @Test
    void createOrder_withValidData_shouldCreateOrder() {
        User client = createAndSaveUser("client1@test.com");
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-001", "Warehouse 1"));
        Product product = productRepository.save(
                new Product("SKU-001", "Product 1", "Description", new BigDecimal("10.00"), true)
        );

        SalesOrderCreateDTO createDTO = new SalesOrderCreateDTO();
        createDTO.setWarehouseId(warehouse.getId());

        SalesOrderLineCreateDTO lineDTO = new SalesOrderLineCreateDTO();
        lineDTO.setProductId(product.getId());
        lineDTO.setQuantity(5);
        createDTO.setLines(List.of(lineDTO));

        SalesOrderDTO result = salesOrderService.createOrder(createDTO, client);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals(SalesOrderStatus.CREATED, result.getStatus());
        assertEquals(1, result.getLines().size());
        assertEquals(5, result.getLines().get(0).getQuantity());
        assertEquals(SalesOrderLineStatus.CREATED, result.getLines().get(0).getStatus());
    }

    @Test
    void createOrder_withMultipleLines_shouldCreateAllLines() {
        User client = createAndSaveUser("client2@test.com");
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-002", "Warehouse 2"));
        Product product1 = productRepository.save(
                new Product("SKU-002", "Product 2", "Description", new BigDecimal("10.00"), true)
        );
        Product product2 = productRepository.save(
                new Product("SKU-003", "Product 3", "Description", new BigDecimal("20.00"), true)
        );

        SalesOrderCreateDTO createDTO = new SalesOrderCreateDTO();
        createDTO.setWarehouseId(warehouse.getId());

        SalesOrderLineCreateDTO line1 = new SalesOrderLineCreateDTO();
        line1.setProductId(product1.getId());
        line1.setQuantity(3);

        SalesOrderLineCreateDTO line2 = new SalesOrderLineCreateDTO();
        line2.setProductId(product2.getId());
        line2.setQuantity(7);

        createDTO.setLines(List.of(line1, line2));

        SalesOrderDTO result = salesOrderService.createOrder(createDTO, client);

        assertNotNull(result);
        assertEquals(2, result.getLines().size());
    }

    @Test
    void createOrder_withNonExistingWarehouse_shouldThrowException() {
        User client = createAndSaveUser("client3@test.com");

        SalesOrderCreateDTO createDTO = new SalesOrderCreateDTO();
        createDTO.setWarehouseId(9999L);
        createDTO.setLines(List.of());

        assertThrows(ResourceNotFoundException.class,
                () -> salesOrderService.createOrder(createDTO, client));
    }

    @Test
    void createOrder_withNonExistingProduct_shouldThrowException() {
        User client = createAndSaveUser("client4@test.com");
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-003", "Warehouse 3"));

        SalesOrderCreateDTO createDTO = new SalesOrderCreateDTO();
        createDTO.setWarehouseId(warehouse.getId());

        SalesOrderLineCreateDTO lineDTO = new SalesOrderLineCreateDTO();
        lineDTO.setProductId(9999L);
        lineDTO.setQuantity(5);
        createDTO.setLines(List.of(lineDTO));

        assertThrows(ResourceNotFoundException.class,
                () -> salesOrderService.createOrder(createDTO, client));
    }

    // ========== TESTS reserveOrderStock ==========

    @Test
    void reserveOrderStock_withSufficientStock_shouldReserveAndUpdateStatus() {
        User client = createAndSaveUser("reserve1@test.com");
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-RES-001", "Warehouse"));
        Product product = productRepository.save(
                new Product("SKU-RES-001", "Product", "Description", new BigDecimal("10.00"), true)
        );
        inventoryRepository.save(new Inventory(product, warehouse, 20, 0));

        SalesOrder order = new SalesOrder();
        order.setClient(client);
        order.setWarehouse(warehouse);
        order.setStatus(SalesOrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());

        SalesOrderLine line = new SalesOrderLine();
        line.setProduct(product);
        line.setQuantity(10);
        line.setStatus(SalesOrderLineStatus.CREATED);
        order.addLine(line);

        SalesOrder savedOrder = salesOrderRepository.save(order);

        SalesOrderDTO result = salesOrderService.reserveOrderStock(savedOrder.getId(), client);

        assertNotNull(result);
        assertEquals(SalesOrderStatus.RESERVED, result.getStatus());
        assertEquals(SalesOrderLineStatus.RESERVED, result.getLines().get(0).getStatus());

        Inventory inventory = inventoryRepository.findByProductAndWarehouse(product, warehouse).orElseThrow();
        assertEquals(10, inventory.getQtyReserved());
    }

    @Test
    void reserveOrderStock_withInsufficientStock_shouldPartiallyReserve() {
        User client = createAndSaveUser("reserve2@test.com");
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-RES-002", "Warehouse"));
        Product product = productRepository.save(
                new Product("SKU-RES-002", "Product", "Description", new BigDecimal("10.00"), true)
        );
        inventoryRepository.save(new Inventory(product, warehouse, 5, 0));

        SalesOrder order = new SalesOrder();
        order.setClient(client);
        order.setWarehouse(warehouse);
        order.setStatus(SalesOrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());

        SalesOrderLine line = new SalesOrderLine();
        line.setProduct(product);
        line.setQuantity(10);
        line.setStatus(SalesOrderLineStatus.CREATED);
        order.addLine(line);

        SalesOrder savedOrder = salesOrderRepository.save(order);

        SalesOrderDTO result = salesOrderService.reserveOrderStock(savedOrder.getId(), client);

        assertNotNull(result);
        assertEquals(SalesOrderStatus.PARTIALLY_RESERVED, result.getStatus());
        assertEquals(SalesOrderLineStatus.BACKORDERED, result.getLines().get(0).getStatus());
    }

    @Test
    void reserveOrderStock_withWrongClient_shouldThrowSecurityException() {
        User client1 = createAndSaveUser("client1@reserve.com");
        User client2 = createAndSaveUser("client2@reserve.com");
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-SEC-001", "Warehouse"));

        SalesOrder order = new SalesOrder();
        order.setClient(client1);
        order.setWarehouse(warehouse);
        order.setStatus(SalesOrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());

        SalesOrder savedOrder = salesOrderRepository.save(order);

        assertThrows(SecurityException.class,
                () -> salesOrderService.reserveOrderStock(savedOrder.getId(), client2));
    }

    @Test
    void reserveOrderStock_withNonCreatedStatus_shouldThrowIllegalStateException() {
        User client = createAndSaveUser("status@test.com");
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-STATUS-001", "Warehouse"));

        SalesOrder order = new SalesOrder();
        order.setClient(client);
        order.setWarehouse(warehouse);
        order.setStatus(SalesOrderStatus.RESERVED);
        order.setCreatedAt(LocalDateTime.now());

        SalesOrder savedOrder = salesOrderRepository.save(order);

        assertThrows(IllegalStateException.class,
                () -> salesOrderService.reserveOrderStock(savedOrder.getId(), client));
    }

    @Test
    void reserveOrderStock_withNonExistingOrder_shouldThrowException() {
        User client = createAndSaveUser("notfound@test.com");

        assertThrows(ResourceNotFoundException.class,
                () -> salesOrderService.reserveOrderStock(9999L, client));
    }

    // ========== TESTS getMyOrder ==========

    @Test
    void getMyOrder_shouldReturnOnlyClientOrders() {
        User client1 = createAndSaveUser("myclient1@test.com");
        User client2 = createAndSaveUser("myclient2@test.com");
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-MY-001", "Warehouse"));

        SalesOrder order1 = createOrder(client1, warehouse);
        SalesOrder order2 = createOrder(client1, warehouse);
        SalesOrder order3 = createOrder(client2, warehouse);

        salesOrderRepository.saveAll(List.of(order1, order2, order3));

        List<SalesOrderDTO> client1Orders = salesOrderService.getMyOrder(client1);
        List<SalesOrderDTO> client2Orders = salesOrderService.getMyOrder(client2);

        assertEquals(2, client1Orders.size());
        assertEquals(1, client2Orders.size());
    }

    @Test
    void getMyOrder_whenNoOrders_shouldReturnEmptyList() {
        User client = createAndSaveUser("noorders@test.com");

        List<SalesOrderDTO> orders = salesOrderService.getMyOrder(client);

        assertNotNull(orders);
        assertTrue(orders.isEmpty());
    }

    // ========== TESTS getOrder ==========

    @Test
    void getOrder_withValidClientAndOrderId_shouldReturnOrder() {
        User client = createAndSaveUser("getorder@test.com");
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-GET-001", "Warehouse"));

        SalesOrder order = createOrder(client, warehouse);
        SalesOrder savedOrder = salesOrderRepository.save(order);

        SalesOrderDTO result = salesOrderService.getOrder(savedOrder.getId(), client);

        assertNotNull(result);
        assertEquals(savedOrder.getId(), result.getId());
    }

    @Test
    void getOrder_withWrongClient_shouldThrowSecurityException() {
        User client1 = createAndSaveUser("order1@test.com");
        User client2 = createAndSaveUser("order2@test.com");
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-GET-002", "Warehouse"));

        SalesOrder order = createOrder(client1, warehouse);
        SalesOrder savedOrder = salesOrderRepository.save(order);

        assertThrows(SecurityException.class,
                () -> salesOrderService.getOrder(savedOrder.getId(), client2));
    }

    @Test
    void getOrder_withNonExistingOrder_shouldThrowException() {
        User client = createAndSaveUser("notexist@test.com");

        assertThrows(ResourceNotFoundException.class,
                () -> salesOrderService.getOrder(9999L, client));
    }

    // ========== Helper Methods ==========

    private User createAndSaveUser(String email) {
        User user = new User();
        user.setEmail(email);
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(Role.CLIENT);
        user.setActive(true);
        return userRepository.save(user);
    }

    private SalesOrder createOrder(User client, Warehouse warehouse) {
        SalesOrder order = new SalesOrder();
        order.setClient(client);
        order.setWarehouse(warehouse);
        order.setStatus(SalesOrderStatus.CREATED);
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }
}