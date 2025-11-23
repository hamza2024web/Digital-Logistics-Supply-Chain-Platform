package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.request.shipment.ShipmentCreateDTO;
import com.spring.digital_logistics.dto.response.shipment.ShipmentDTO;
import com.spring.digital_logistics.entity.*;
import com.spring.digital_logistics.entity.enums.SalesOrderStatus;
import com.spring.digital_logistics.entity.enums.ShipmentStatus;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.mapper.ShipmentMapper;
import com.spring.digital_logistics.repository.SalesOrderRepository;
import com.spring.digital_logistics.repository.ShipmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShipmentServiceTest {

    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private ShipmentMapper shipmentMapper;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private ShipmentService shipmentService;

    private SalesOrder salesOrder;
    private Shipment shipment;
    private ShipmentCreateDTO createDTO;

    @BeforeEach
    void setUp() {
        User client = new User();
        client.setId(1L);

        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);

        salesOrder = new SalesOrder();
        salesOrder.setId(1L);
        salesOrder.setClient(client);
        salesOrder.setWarehouse(warehouse);
        salesOrder.setStatus(SalesOrderStatus.RESERVED);
        salesOrder.setCreatedAt(LocalDateTime.now());

        shipment = new Shipment();
        shipment.setId(1L);
        shipment.setTrackingNumber("TRACK-001");
        shipment.setStatus(ShipmentStatus.PLANNED);
        shipment.setCreationDate(LocalDateTime.now());

        createDTO = new ShipmentCreateDTO();
        createDTO.setTrackingNumber("TRACK-001");
    }

    // ========== TESTS createAndPlanShipment ==========

    @Test
    void createAndPlanShipment_withValidOrder_shouldCreateShipment() {
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(salesOrder));
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(shipment);
        when(salesOrderRepository.save(salesOrder)).thenReturn(salesOrder);

        ShipmentDTO expectedDTO = new ShipmentDTO();
        when(shipmentMapper.toDto(shipment)).thenReturn(expectedDTO);

        ShipmentDTO result = shipmentService.createAndPlanShipment(1L, createDTO);

        assertNotNull(result);
        verify(salesOrderRepository).findById(1L);
        verify(shipmentRepository).save(any(Shipment.class));
        verify(salesOrderRepository).save(salesOrder);
    }

    @Test
    void createAndPlanShipment_withNonReservedOrder_shouldThrowException() {
        salesOrder.setStatus(SalesOrderStatus.CREATED);
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(salesOrder));

        assertThrows(IllegalStateException.class,
                () -> shipmentService.createAndPlanShipment(1L, createDTO));
        verify(shipmentRepository, never()).save(any());
    }

    @Test
    void createAndPlanShipment_whenShipmentAlreadyExists_shouldThrowException() {
        salesOrder.setShipment(shipment);
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(salesOrder));

        assertThrows(IllegalStateException.class,
                () -> shipmentService.createAndPlanShipment(1L, createDTO));
        verify(shipmentRepository, never()).save(any());
    }

    @Test
    void createAndPlanShipment_withNonExistingOrder_shouldThrowException() {
        when(salesOrderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> shipmentService.createAndPlanShipment(999L, createDTO));
    }

    // ========== TESTS shipOrder ==========

    @Test
    void shipOrder_withValidOrder_shouldUpdateStatusAndCallInventory() {
        salesOrder.setShipment(shipment);
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(salesOrder));
        doNothing().when(inventoryService).recordOutboundMovementForOrder(salesOrder);
        when(salesOrderRepository.save(salesOrder)).thenReturn(salesOrder);
        when(shipmentRepository.save(shipment)).thenReturn(shipment);

        ShipmentDTO expectedDTO = new ShipmentDTO();
        when(shipmentMapper.toDto(shipment)).thenReturn(expectedDTO);

        ShipmentDTO result = shipmentService.shipOrder(1L);

        assertNotNull(result);
        assertEquals(SalesOrderStatus.SHIPPED, salesOrder.getStatus());
        assertEquals(ShipmentStatus.IN_TRANSIT, shipment.getStatus());
        verify(inventoryService).recordOutboundMovementForOrder(salesOrder);
        verify(salesOrderRepository).save(salesOrder);
        verify(shipmentRepository).save(shipment);
    }

    @Test
    void shipOrder_whenNoShipment_shouldThrowException() {
        salesOrder.setShipment(null);
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(salesOrder));

        assertThrows(IllegalStateException.class,
                () -> shipmentService.shipOrder(1L));
        verify(inventoryService, never()).recordOutboundMovementForOrder(any());
    }

    @Test
    void shipOrder_withWrongOrderStatus_shouldThrowException() {
        salesOrder.setShipment(shipment);
        salesOrder.setStatus(SalesOrderStatus.CREATED);
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(salesOrder));

        assertThrows(IllegalStateException.class,
                () -> shipmentService.shipOrder(1L));
        verify(inventoryService, never()).recordOutboundMovementForOrder(any());
    }

    @Test
    void shipOrder_withWrongShipmentStatus_shouldThrowException() {
        salesOrder.setShipment(shipment);
        shipment.setStatus(ShipmentStatus.IN_TRANSIT);
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(salesOrder));

        assertThrows(IllegalStateException.class,
                () -> shipmentService.shipOrder(1L));
        verify(inventoryService, never()).recordOutboundMovementForOrder(any());
    }

    @Test
    void shipOrder_withNonExistingOrder_shouldThrowException() {
        when(salesOrderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> shipmentService.shipOrder(999L));
    }

    // ========== TESTS deliverOrder ==========

    @Test
    void deliverOrder_withValidOrder_shouldUpdateStatus() {
        salesOrder.setStatus(SalesOrderStatus.SHIPPED);
        salesOrder.setShipment(shipment);
        shipment.setStatus(ShipmentStatus.IN_TRANSIT);

        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(salesOrder));
        when(salesOrderRepository.save(salesOrder)).thenReturn(salesOrder);
        when(shipmentRepository.save(shipment)).thenReturn(shipment);

        ShipmentDTO expectedDTO = new ShipmentDTO();
        when(shipmentMapper.toDto(shipment)).thenReturn(expectedDTO);

        ShipmentDTO result = shipmentService.deliverOrder(1L);

        assertNotNull(result);
        assertEquals(SalesOrderStatus.DELIVERED, salesOrder.getStatus());
        assertEquals(ShipmentStatus.DELIVERED, shipment.getStatus());
        verify(salesOrderRepository).save(salesOrder);
        verify(shipmentRepository).save(shipment);
    }

    @Test
    void deliverOrder_whenNoShipment_shouldThrowException() {
        salesOrder.setShipment(null);
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(salesOrder));

        assertThrows(IllegalStateException.class,
                () -> shipmentService.deliverOrder(1L));
    }

    @Test
    void deliverOrder_withWrongOrderStatus_shouldThrowException() {
        salesOrder.setStatus(SalesOrderStatus.RESERVED);
        salesOrder.setShipment(shipment);
        shipment.setStatus(ShipmentStatus.IN_TRANSIT);
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(salesOrder));

        assertThrows(IllegalStateException.class,
                () -> shipmentService.deliverOrder(1L));
    }

    @Test
    void deliverOrder_withWrongShipmentStatus_shouldThrowException() {
        salesOrder.setStatus(SalesOrderStatus.SHIPPED);
        salesOrder.setShipment(shipment);
        shipment.setStatus(ShipmentStatus.PLANNED);
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(salesOrder));

        assertThrows(IllegalStateException.class,
                () -> shipmentService.deliverOrder(1L));
    }

    @Test
    void deliverOrder_withNonExistingOrder_shouldThrowException() {
        when(salesOrderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> shipmentService.deliverOrder(999L));
    }
}