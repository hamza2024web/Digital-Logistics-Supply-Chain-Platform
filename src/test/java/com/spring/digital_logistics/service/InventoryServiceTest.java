package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.request.adjustement.AdjustmentRequestDTO;
import com.spring.digital_logistics.dto.request.inventory.MovementRequestDTO;
import com.spring.digital_logistics.dto.response.inventory.InventoryDTO;
import com.spring.digital_logistics.entity.*;
import com.spring.digital_logistics.entity.enums.MovementType;
import com.spring.digital_logistics.entity.enums.SalesOrderLineStatus;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.exception.StockUnavailableException;
import com.spring.digital_logistics.mapper.InventoryMapper;
import com.spring.digital_logistics.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private InventoryMovementRepository inventoryMovementRepository;
    @Mock
    private InventoryMapper inventoryMapper;

    @InjectMocks
    private InventoryService inventoryService;

    private Product product;
    private Warehouse warehouse;
    private Inventory inventory;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setSku("TSHIRT-BLEU");

        warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setCode("W-PARIS");

        inventory = new Inventory(product, warehouse, 10, 0);
    }

    // ========== TESTS recordInboundMovement ==========

    @Test
    void recordInboundMovement_whenInventoryExists_shouldIncreaseStock() {
        MovementRequestDTO request = new MovementRequestDTO();
        request.setProductId(1L);
        request.setWarehouseId(1L);
        request.setQuantity(5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        InventoryDTO expectedDTO = new InventoryDTO();
        when(inventoryMapper.toDto(any(Inventory.class))).thenReturn(expectedDTO);

        InventoryDTO result = inventoryService.recordInboundMovement(request);

        assertNotNull(result);
        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(inventoryCaptor.capture());

        Inventory savedInventory = inventoryCaptor.getValue();
        assertEquals(15, savedInventory.getQtyOnHand());

        ArgumentCaptor<InventoryMovement> movementCaptor = ArgumentCaptor.forClass(InventoryMovement.class);
        verify(inventoryMovementRepository).save(movementCaptor.capture());

        InventoryMovement savedMovement = movementCaptor.getValue();
        assertEquals(MovementType.INBOUND, savedMovement.getType());
        assertEquals(5, savedMovement.getQty());
    }

    @Test
    void recordInboundMovement_whenInventoryDoesNotExist_shouldCreateNewInventory() {
        MovementRequestDTO request = new MovementRequestDTO();
        request.setProductId(1L);
        request.setWarehouseId(1L);
        request.setQuantity(5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse))
                .thenReturn(Optional.empty());

        Inventory newInventory = new Inventory(product, warehouse, 5, 0);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(newInventory);

        InventoryDTO expectedDTO = new InventoryDTO();
        when(inventoryMapper.toDto(any(Inventory.class))).thenReturn(expectedDTO);

        InventoryDTO result = inventoryService.recordInboundMovement(request);

        assertNotNull(result);
        verify(inventoryRepository).save(any(Inventory.class));
        verify(inventoryMovementRepository).save(any(InventoryMovement.class));
    }

    @Test
    void recordInboundMovement_whenProductNotFound_shouldThrowException() {
        MovementRequestDTO request = new MovementRequestDTO();
        request.setProductId(999L);
        request.setWarehouseId(1L);
        request.setQuantity(5);

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> inventoryService.recordInboundMovement(request));
    }

    @Test
    void recordInboundMovement_whenWarehouseNotFound_shouldThrowException() {
        MovementRequestDTO request = new MovementRequestDTO();
        request.setProductId(1L);
        request.setWarehouseId(999L);
        request.setQuantity(5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> inventoryService.recordInboundMovement(request));
    }

    // ========== TESTS recordOutBoundMovement ==========

    @Test
    void recordOutBoundMovement_whenStockIsSufficient_shouldDecreaseStock() {
        MovementRequestDTO request = new MovementRequestDTO();
        request.setProductId(1L);
        request.setWarehouseId(1L);
        request.setQuantity(5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        InventoryDTO expectedDTO = new InventoryDTO();
        when(inventoryMapper.toDto(any(Inventory.class))).thenReturn(expectedDTO);

        InventoryDTO result = inventoryService.recordOutBoundMovement(request);

        assertNotNull(result);
        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(inventoryCaptor.capture());

        Inventory savedInventory = inventoryCaptor.getValue();
        assertEquals(5, savedInventory.getQtyOnHand());

        ArgumentCaptor<InventoryMovement> movementCaptor = ArgumentCaptor.forClass(InventoryMovement.class);
        verify(inventoryMovementRepository).save(movementCaptor.capture());

        InventoryMovement savedMovement = movementCaptor.getValue();
        assertEquals(MovementType.OUTBOUND, savedMovement.getType());
        assertEquals(5, savedMovement.getQty());
    }

    @Test
    void recordOutBoundMovement_whenInventoryNotFound_shouldThrowException() {
        MovementRequestDTO request = new MovementRequestDTO();
        request.setProductId(1L);
        request.setWarehouseId(1L);
        request.setQuantity(5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse))
                .thenReturn(Optional.empty());

        assertThrows(StockUnavailableException.class,
                () -> inventoryService.recordOutBoundMovement(request));
    }

    @Test
    void recordOutBoundMovement_whenStockIsInsufficient_shouldThrowException() {
        MovementRequestDTO request = new MovementRequestDTO();
        request.setProductId(1L);
        request.setWarehouseId(1L);
        request.setQuantity(15);

        inventory.setQtyOnHand(10);
        inventory.setQtyReserved(0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse))
                .thenReturn(Optional.of(inventory));

        StockUnavailableException exception = assertThrows(StockUnavailableException.class,
                () -> inventoryService.recordOutBoundMovement(request));

        assertTrue(exception.getMessage().contains("Stock disponible insuffisant"));
    }

    @Test
    void recordOutBoundMovement_whenStockIsReserved_shouldConsiderAvailableStock() {
        MovementRequestDTO request = new MovementRequestDTO();
        request.setProductId(1L);
        request.setWarehouseId(1L);
        request.setQuantity(8);

        inventory.setQtyOnHand(10);
        inventory.setQtyReserved(5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse))
                .thenReturn(Optional.of(inventory));

        assertThrows(StockUnavailableException.class,
                () -> inventoryService.recordOutBoundMovement(request));
    }

    // ========== TESTS recordAdjustement ==========

    @Test
    void recordAdjustement_withPositiveAdjustment_shouldIncreaseStock() {
        AdjustmentRequestDTO request = new AdjustmentRequestDTO();
        request.setProductId(1L);
        request.setWarehouseId(1L);
        request.setQuantity(5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        InventoryDTO expectedDTO = new InventoryDTO();
        when(inventoryMapper.toDto(any(Inventory.class))).thenReturn(expectedDTO);

        InventoryDTO result = inventoryService.recordAdjustement(request);

        assertNotNull(result);
        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(inventoryCaptor.capture());

        Inventory savedInventory = inventoryCaptor.getValue();
        assertEquals(15, savedInventory.getQtyOnHand());
    }

    @Test
    void recordAdjustement_withNegativeAdjustment_shouldDecreaseStock() {
        AdjustmentRequestDTO request = new AdjustmentRequestDTO();
        request.setProductId(1L);
        request.setWarehouseId(1L);
        request.setQuantity(-5);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse))
                .thenReturn(Optional.of(inventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(inventory);

        InventoryDTO expectedDTO = new InventoryDTO();
        when(inventoryMapper.toDto(any(Inventory.class))).thenReturn(expectedDTO);

        InventoryDTO result = inventoryService.recordAdjustement(request);

        assertNotNull(result);
        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(inventoryCaptor.capture());

        Inventory savedInventory = inventoryCaptor.getValue();
        assertEquals(5, savedInventory.getQtyOnHand());
    }

    @Test
    void recordAdjustement_withNegativeAdjustmentExceedingAvailableStock_shouldThrowException() {
        AdjustmentRequestDTO request = new AdjustmentRequestDTO();
        request.setProductId(1L);
        request.setWarehouseId(1L);
        request.setQuantity(-15);

        inventory.setQtyOnHand(10);
        inventory.setQtyReserved(0);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse))
                .thenReturn(Optional.of(inventory));

        assertThrows(StockUnavailableException.class,
                () -> inventoryService.recordAdjustement(request));
    }

    @Test
    void recordAdjustement_whenInventoryDoesNotExist_shouldCreateNewInventory() {
        AdjustmentRequestDTO request = new AdjustmentRequestDTO();
        request.setProductId(1L);
        request.setWarehouseId(1L);
        request.setQuantity(10);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse))
                .thenReturn(Optional.empty());

        Inventory newInventory = new Inventory(product, warehouse, 10, 0);
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(newInventory);

        InventoryDTO expectedDTO = new InventoryDTO();
        when(inventoryMapper.toDto(any(Inventory.class))).thenReturn(expectedDTO);

        InventoryDTO result = inventoryService.recordAdjustement(request);

        assertNotNull(result);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    // ========== TESTS reserveStockForOrder ==========

    @Test
    void reserveStockForOrder_whenStockIsSufficient_shouldReserveStockAndReturnTrue() {
        SalesOrder order = new SalesOrder();
        order.setId(1L);
        order.setWarehouse(warehouse);
        SalesOrderLine line = new SalesOrderLine();
        line.setProduct(product);
        line.setQuantity(5);
        order.addLine(line);

        when(inventoryRepository.findByProductAndWarehouse(product, warehouse))
                .thenReturn(Optional.of(inventory));

        boolean result = inventoryService.reserveStockForOrder(order);

        assertTrue(result);
        assertEquals(SalesOrderLineStatus.RESERVED, line.getStatus());

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(inventoryCaptor.capture());

        Inventory savedInventory = inventoryCaptor.getValue();
        assertEquals(5, savedInventory.getQtyReserved());
    }

    @Test
    void reserveStockForOrder_whenStockIsInsufficient_shouldBackorderAndReturnFalse() {
        SalesOrder order = new SalesOrder();
        order.setId(1L);
        order.setWarehouse(warehouse);
        SalesOrderLine line = new SalesOrderLine();
        line.setProduct(product);
        line.setQuantity(10);
        order.addLine(line);

        Inventory insufficientInventory = new Inventory(product, warehouse, 8, 3);
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse))
                .thenReturn(Optional.of(insufficientInventory));

        boolean result = inventoryService.reserveStockForOrder(order);

        assertFalse(result);
        assertEquals(SalesOrderLineStatus.BACKORDERED, line.getStatus());
        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void reserveStockForOrder_withMultipleLines_andMixedStock_shouldPartiallyReserveAndReturnFalse() {
        Product productB = new Product();
        productB.setId(2L);
        productB.setSku("JEANS-NOIR");

        SalesOrder order = new SalesOrder();
        order.setId(1L);
        order.setWarehouse(warehouse);

        SalesOrderLine lineA = new SalesOrderLine();
        lineA.setProduct(product);
        lineA.setQuantity(5);
        order.addLine(lineA);

        SalesOrderLine lineB = new SalesOrderLine();
        lineB.setProduct(productB);
        lineB.setQuantity(10);
        order.addLine(lineB);

        Inventory inventoryA = new Inventory(product, warehouse, 10, 0);
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse))
                .thenReturn(Optional.of(inventoryA));

        Inventory inventoryB = new Inventory(productB, warehouse, 5, 3);
        when(inventoryRepository.findByProductAndWarehouse(productB, warehouse))
                .thenReturn(Optional.of(inventoryB));

        boolean result = inventoryService.reserveStockForOrder(order);

        assertFalse(result);
        assertEquals(SalesOrderLineStatus.RESERVED, lineA.getStatus());
        assertEquals(SalesOrderLineStatus.BACKORDERED, lineB.getStatus());

        verify(inventoryRepository, times(1)).save(any(Inventory.class));
    }

    // ========== TESTS recordOutboundMovementForOrder ==========

    @Test
    void recordOutboundMovementForOrder_shouldDecreaseStockAndReservation() {
        SalesOrder order = new SalesOrder();
        order.setId(1L);
        order.setWarehouse(warehouse);

        SalesOrderLine line = new SalesOrderLine();
        line.setProduct(product);
        line.setQuantity(5);
        order.addLine(line);

        inventory.setQtyOnHand(10);
        inventory.setQtyReserved(5);

        when(inventoryRepository.findByProductAndWarehouse(product, warehouse))
                .thenReturn(Optional.of(inventory));

        inventoryService.recordOutboundMovementForOrder(order);

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(inventoryCaptor.capture());

        Inventory savedInventory = inventoryCaptor.getValue();
        assertEquals(5, savedInventory.getQtyOnHand());
        assertEquals(0, savedInventory.getQtyReserved());

        verify(inventoryMovementRepository).save(any(InventoryMovement.class));
    }

    @Test
    void recordOutboundMovementForOrder_whenInventoryNotFound_shouldThrowException() {
        SalesOrder order = new SalesOrder();
        order.setId(1L);
        order.setWarehouse(warehouse);

        SalesOrderLine line = new SalesOrderLine();
        line.setProduct(product);
        line.setQuantity(5);
        order.addLine(line);

        when(inventoryRepository.findByProductAndWarehouse(product, warehouse))
                .thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class,
                () -> inventoryService.recordOutboundMovementForOrder(order));
    }

    @Test
    void recordOutboundMovementForOrder_whenStockInconsistent_shouldThrowException() {
        SalesOrder order = new SalesOrder();
        order.setId(1L);
        order.setWarehouse(warehouse);

        SalesOrderLine line = new SalesOrderLine();
        line.setProduct(product);
        line.setQuantity(10);
        order.addLine(line);

        inventory.setQtyOnHand(5);
        inventory.setQtyReserved(10);

        when(inventoryRepository.findByProductAndWarehouse(product, warehouse))
                .thenReturn(Optional.of(inventory));

        assertThrows(IllegalStateException.class,
                () -> inventoryService.recordOutboundMovementForOrder(order));
    }

    // ========== TESTS shipStock ==========

    @Test
    void shipStock_shouldDecreaseSourceWarehouseStock() {
        inventory.setQtyOnHand(20);

        when(inventoryRepository.findByProductAndWarehouse(product, warehouse))
                .thenReturn(Optional.of(inventory));

        inventoryService.shipStock(product, warehouse, 5);

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(inventoryCaptor.capture());

        Inventory savedInventory = inventoryCaptor.getValue();
        assertEquals(15, savedInventory.getQtyOnHand());
    }

    @Test
    void shipStock_whenInventoryNotFound_shouldThrowException() {
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> inventoryService.shipStock(product, warehouse, 5));
    }

    // ========== TESTS receiveStock ==========

    @Test
    void receiveStock_shouldIncreaseDestinationWarehouseStock() {
        inventory.setQtyOnHand(10);

        when(inventoryRepository.findByProductAndWarehouse(product, warehouse))
                .thenReturn(Optional.of(inventory));

        inventoryService.receiveStock(product, warehouse, 5);

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(inventoryCaptor.capture());

        Inventory savedInventory = inventoryCaptor.getValue();
        assertEquals(15, savedInventory.getQtyOnHand());
    }

    @Test
    void receiveStock_whenInventoryNotFound_shouldThrowException() {
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> inventoryService.receiveStock(product, warehouse, 5));
    }
}