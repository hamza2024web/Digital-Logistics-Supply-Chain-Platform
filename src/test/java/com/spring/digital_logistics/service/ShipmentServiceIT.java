package com.spring.digital_logistics.service;

import com.spring.digital_logistics.IntegrationTestBase;
import com.spring.digital_logistics.dto.request.shipment.ShipmentCreateDTO;
import com.spring.digital_logistics.dto.response.shipment.ShipmentDTO;
import com.spring.digital_logistics.entity.*;
import com.spring.digital_logistics.entity.enums.Role;
import com.spring.digital_logistics.entity.enums.SalesOrderLineStatus;
import com.spring.digital_logistics.entity.enums.SalesOrderStatus;
import com.spring.digital_logistics.entity.enums.ShipmentStatus;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.repository.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class ShipmentServiceIT extends IntegrationTestBase {

    @Autowired
    private ShipmentService shipmentService;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private ShipmentRepository shipmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    // ========== TESTS createAndPlanShipment ==========

    @Test
    void createAndPlanShipment_withValidOrder_shouldCreateShipmentInDatabase() {
        SalesOrder order = createReservedOrder();

        ShipmentCreateDTO createDTO = new ShipmentCreateDTO();
        createDTO.setTrackingNumber("TRACK-001");

        ShipmentDTO result = shipmentService.createAndPlanShipment(order.getId(), createDTO);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("TRACK-001", result.getTrackingNumber());
        assertEquals(ShipmentStatus.PLANNED, result.getStatus());

        SalesOrder updatedOrder = salesOrderRepository.findById(order.getId()).orElseThrow();
        assertNotNull(updatedOrder.getShipment());
        assertEquals("TRACK-001", updatedOrder.getShipment().getTrackingNumber());
    }

    @Test
    void createAndPlanShipment_withNonReservedOrder_shouldThrowException() {
        SalesOrder order = createOrderWithStatus(SalesOrderStatus.CREATED);

        ShipmentCreateDTO createDTO = new ShipmentCreateDTO();
        createDTO.setTrackingNumber("TRACK-002");

        assertThrows(IllegalStateException.class,
                () -> shipmentService.createAndPlanShipment(order.getId(), createDTO));
    }

    @Test
    void createAndPlanShipment_whenShipmentAlreadyExists_shouldThrowException() {
        SalesOrder order = createReservedOrder();

        Shipment existingShipment = new Shipment();
        existingShipment.setTrackingNumber("EXISTING");
        existingShipment.setStatus(ShipmentStatus.PLANNED);
        existingShipment.setCreationDate(LocalDateTime.now());
        shipmentRepository.save(existingShipment);

        order.setShipment(existingShipment);
        salesOrderRepository.save(order);

        ShipmentCreateDTO createDTO = new ShipmentCreateDTO();
        createDTO.setTrackingNumber("TRACK-003");

        assertThrows(IllegalStateException.class,
                () -> shipmentService.createAndPlanShipment(order.getId(), createDTO));
    }

    @Test
    void createAndPlanShipment_withNonExistingOrder_shouldThrowException() {
        ShipmentCreateDTO createDTO = new ShipmentCreateDTO();
        createDTO.setTrackingNumber("TRACK-004");

        assertThrows(ResourceNotFoundException.class,
                () -> shipmentService.createAndPlanShipment(9999L, createDTO));
    }

    // ========== TESTS shipOrder ==========

    @Test
    void shipOrder_withValidOrder_shouldUpdateStatusesAndInventory() {
        SalesOrder order = createReservedOrderWithInventory();

        Shipment shipment = new Shipment();
        shipment.setTrackingNumber("SHIP-001");
        shipment.setStatus(ShipmentStatus.PLANNED);
        shipment.setCreationDate(LocalDateTime.now());
        shipmentRepository.save(shipment);

        order.setShipment(shipment);
        salesOrderRepository.save(order);

        ShipmentDTO result = shipmentService.shipOrder(order.getId());

        assertNotNull(result);
        assertEquals(ShipmentStatus.IN_TRANSIT, result.getStatus());

        SalesOrder updatedOrder = salesOrderRepository.findById(order.getId()).orElseThrow();
        assertEquals(SalesOrderStatus.SHIPPED, updatedOrder.getStatus());
        assertEquals(ShipmentStatus.IN_TRANSIT, updatedOrder.getShipment().getStatus());

        Product product = updatedOrder.getLines().get(0).getProduct();
        Warehouse warehouse = updatedOrder.getWarehouse();
        Inventory inventory = inventoryRepository.findByProductAndWarehouse(product, warehouse).orElseThrow();
        assertEquals(5, inventory.getQtyOnHand());
        assertEquals(0, inventory.getQtyReserved());
    }

    @Test
    void shipOrder_whenNoShipment_shouldThrowException() {
        SalesOrder order = createReservedOrder();

        assertThrows(IllegalStateException.class,
                () -> shipmentService.shipOrder(order.getId()));
    }

    @Test
    void shipOrder_withWrongOrderStatus_shouldThrowException() {
        SalesOrder order = createOrderWithStatus(SalesOrderStatus.CREATED);

        Shipment shipment = new Shipment();
        shipment.setTrackingNumber("WRONG-001");
        shipment.setStatus(ShipmentStatus.PLANNED);
        shipment.setCreationDate(LocalDateTime.now());
        shipmentRepository.save(shipment);

        order.setShipment(shipment);
        salesOrderRepository.save(order);

        assertThrows(IllegalStateException.class,
                () -> shipmentService.shipOrder(order.getId()));
    }

    @Test
    void shipOrder_withNonExistingOrder_shouldThrowException() {
        assertThrows(ResourceNotFoundException.class,
                () -> shipmentService.shipOrder(9999L));
    }

    // ========== TESTS deliverOrder ==========

    @Test
    void deliverOrder_withValidOrder_shouldUpdateStatuses() {
        SalesOrder order = createShippedOrder();

        ShipmentDTO result = shipmentService.deliverOrder(order.getId());

        assertNotNull(result);
        assertEquals(ShipmentStatus.DELIVERED, result.getStatus());

        SalesOrder updatedOrder = salesOrderRepository.findById(order.getId()).orElseThrow();
        assertEquals(SalesOrderStatus.DELIVERED, updatedOrder.getStatus());
        assertEquals(ShipmentStatus.DELIVERED, updatedOrder.getShipment().getStatus());
    }

    @Test
    void deliverOrder_whenNoShipment_shouldThrowException() {
        SalesOrder order = createReservedOrder();

        assertThrows(IllegalStateException.class,
                () -> shipmentService.deliverOrder(order.getId()));
    }

    @Test
    void deliverOrder_withWrongOrderStatus_shouldThrowException() {
        SalesOrder order = createReservedOrder();

        Shipment shipment = new Shipment();
        shipment.setTrackingNumber("DELIVER-001");
        shipment.setStatus(ShipmentStatus.IN_TRANSIT);
        shipment.setCreationDate(LocalDateTime.now());
        shipmentRepository.save(shipment);

        order.setShipment(shipment);
        salesOrderRepository.save(order);

        assertThrows(IllegalStateException.class,
                () -> shipmentService.deliverOrder(order.getId()));
    }

    @Test
    void deliverOrder_withWrongShipmentStatus_shouldThrowException() {
        SalesOrder order = createOrderWithStatus(SalesOrderStatus.SHIPPED);

        Shipment shipment = new Shipment();
        shipment.setTrackingNumber("DELIVER-002");
        shipment.setStatus(ShipmentStatus.PLANNED);
        shipment.setCreationDate(LocalDateTime.now());
        shipmentRepository.save(shipment);

        order.setShipment(shipment);
        salesOrderRepository.save(order);

        assertThrows(IllegalStateException.class,
                () -> shipmentService.deliverOrder(order.getId()));
    }

    @Test
    void deliverOrder_withNonExistingOrder_shouldThrowException() {
        assertThrows(ResourceNotFoundException.class,
                () -> shipmentService.deliverOrder(9999L));
    }

    // ========== Helper Methods ==========

    private User createUser() {
        User user = new User();
        user.setEmail("shipment@test.com");
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setRole(Role.CLIENT);
        user.setActive(true);
        return userRepository.save(user);
    }

    private SalesOrder createOrderWithStatus(SalesOrderStatus status) {
        User client = createUser();
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-SHIP-001", "Warehouse"));

        SalesOrder order = new SalesOrder();
        order.setClient(client);
        order.setWarehouse(warehouse);
        order.setStatus(status);
        order.setCreatedAt(LocalDateTime.now());

        return salesOrderRepository.save(order);
    }

    private SalesOrder createReservedOrder() {
        return createOrderWithStatus(SalesOrderStatus.RESERVED);
    }

    private SalesOrder createReservedOrderWithInventory() {
        User client = createUser();
        Warehouse warehouse = warehouseRepository.save(new Warehouse("W-SHIP-INV", "Warehouse"));
        Product product = productRepository.save(
                new Product("SKU-SHIP", "Product", "Description", new BigDecimal("10.00"), true)
        );
        inventoryRepository.save(new Inventory(product, warehouse, 15, 10));

        SalesOrder order = new SalesOrder();
        order.setClient(client);
        order.setWarehouse(warehouse);
        order.setStatus(SalesOrderStatus.RESERVED);
        order.setCreatedAt(LocalDateTime.now());

        SalesOrderLine line = new SalesOrderLine();
        line.setProduct(product);
        line.setQuantity(10);
        line.setStatus(SalesOrderLineStatus.RESERVED);
        order.addLine(line);

        return salesOrderRepository.save(order);
    }

    private SalesOrder createShippedOrder() {
        SalesOrder order = createOrderWithStatus(SalesOrderStatus.SHIPPED);

        Shipment shipment = new Shipment();
        shipment.setTrackingNumber("SHIPPED-001");
        shipment.setStatus(ShipmentStatus.IN_TRANSIT);
        shipment.setCreationDate(LocalDateTime.now());
        shipmentRepository.save(shipment);

        order.setShipment(shipment);
        return salesOrderRepository.save(order);
    }
}