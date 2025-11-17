package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.request.salesOrder.SalesOrderCreateDTO;
import com.spring.digital_logistics.dto.request.salesOrder.SalesOrderLineCreateDTO;
import com.spring.digital_logistics.dto.response.salesOrder.SalesOrderDTO;
import com.spring.digital_logistics.entity.*;
import com.spring.digital_logistics.entity.enums.SalesOrderStatus;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.mapper.SalesOrderMapper;
import com.spring.digital_logistics.repository.ProductRepository;
import com.spring.digital_logistics.repository.SalesOrderRepository;
import com.spring.digital_logistics.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesOrderServiceTest {

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private SalesOrderMapper salesOrderMapper;

    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private SalesOrderService salesOrderService;

    private User client;
    private Warehouse warehouse;
    private Product product;
    private SalesOrder salesOrder;
    private SalesOrderDTO salesOrderDTO;

    @BeforeEach
    void setUp() {
        client = new User();
        client.setId(1L);
        client.setEmail("client@test.com");

        warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setCode("W-001");

        product = new Product();
        product.setId(1L);
        product.setSku("PROD-001");

        salesOrder = new SalesOrder();
        salesOrder.setId(1L);
        salesOrder.setClient(client);
        salesOrder.setWarehouse(warehouse);
        salesOrder.setStatus(SalesOrderStatus.CREATED);

        salesOrderDTO = new SalesOrderDTO();
        salesOrderDTO.setId(1L);
        salesOrderDTO.setStatus(SalesOrderStatus.CREATED);
    }

    // ========== TESTS createOrder ==========

    @Test
    void createOrder_shouldCreateOrderSuccessfully() {
        SalesOrderCreateDTO createDTO = new SalesOrderCreateDTO();
        createDTO.setWarehouseId(1L);

        SalesOrderLineCreateDTO lineDTO = new SalesOrderLineCreateDTO();
        lineDTO.setProductId(1L);
        lineDTO.setQuantity(5);
        createDTO.setLines(Arrays.asList(lineDTO));

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);
        when(salesOrderMapper.toDto(salesOrder)).thenReturn(salesOrderDTO);

        SalesOrderDTO result = salesOrderService.createOrder(createDTO, client);

        assertNotNull(result);
        assertEquals(1L, result.getId());

        ArgumentCaptor<SalesOrder> orderCaptor = ArgumentCaptor.forClass(SalesOrder.class);
        verify(salesOrderRepository).save(orderCaptor.capture());

        SalesOrder capturedOrder = orderCaptor.getValue();
        assertEquals(client, capturedOrder.getClient());
        assertEquals(warehouse, capturedOrder.getWarehouse());
        assertEquals(SalesOrderStatus.CREATED, capturedOrder.getStatus());
        assertEquals(1, capturedOrder.getLines().size());
    }

    @Test
    void createOrder_whenWarehouseNotFound_shouldThrowException() {
        SalesOrderCreateDTO createDTO = new SalesOrderCreateDTO();
        createDTO.setWarehouseId(999L);
        createDTO.setLines(Arrays.asList());

        when(warehouseRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> salesOrderService.createOrder(createDTO, client));
    }

    @Test
    void createOrder_whenProductNotFound_shouldThrowException() {
        SalesOrderCreateDTO createDTO = new SalesOrderCreateDTO();
        createDTO.setWarehouseId(1L);

        SalesOrderLineCreateDTO lineDTO = new SalesOrderLineCreateDTO();
        lineDTO.setProductId(999L);
        lineDTO.setQuantity(5);
        createDTO.setLines(Arrays.asList(lineDTO));

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> salesOrderService.createOrder(createDTO, client));
    }

    @Test
    void createOrder_withMultipleLines_shouldAddAllLines() {
        Product product2 = new Product();
        product2.setId(2L);
        product2.setSku("PROD-002");

        SalesOrderCreateDTO createDTO = new SalesOrderCreateDTO();
        createDTO.setWarehouseId(1L);

        SalesOrderLineCreateDTO lineDTO1 = new SalesOrderLineCreateDTO();
        lineDTO1.setProductId(1L);
        lineDTO1.setQuantity(5);

        SalesOrderLineCreateDTO lineDTO2 = new SalesOrderLineCreateDTO();
        lineDTO2.setProductId(2L);
        lineDTO2.setQuantity(3);

        createDTO.setLines(Arrays.asList(lineDTO1, lineDTO2));

        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.findById(2L)).thenReturn(Optional.of(product2));
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);
        when(salesOrderMapper.toDto(any(SalesOrder.class))).thenReturn(salesOrderDTO);

        salesOrderService.createOrder(createDTO, client);

        ArgumentCaptor<SalesOrder> orderCaptor = ArgumentCaptor.forClass(SalesOrder.class);
        verify(salesOrderRepository).save(orderCaptor.capture());

        SalesOrder capturedOrder = orderCaptor.getValue();
        assertEquals(2, capturedOrder.getLines().size());
    }

    // ========== TESTS reserveOrderStock ==========

    @Test
    void reserveOrderStock_whenStockSufficient_shouldSetStatusToReserved() {
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(salesOrder));
        when(inventoryService.reserveStockForOrder(salesOrder)).thenReturn(true);
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);
        when(salesOrderMapper.toDto(any(SalesOrder.class))).thenReturn(salesOrderDTO);

        SalesOrderDTO result = salesOrderService.reserveOrderStock(1L, client);

        assertNotNull(result);

        ArgumentCaptor<SalesOrder> orderCaptor = ArgumentCaptor.forClass(SalesOrder.class);
        verify(salesOrderRepository).save(orderCaptor.capture());

        SalesOrder capturedOrder = orderCaptor.getValue();
        assertEquals(SalesOrderStatus.RESERVED, capturedOrder.getStatus());
    }

    @Test
    void reserveOrderStock_whenStockPartial_shouldSetStatusToPartiallyReserved() {
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(salesOrder));
        when(inventoryService.reserveStockForOrder(salesOrder)).thenReturn(false);
        when(salesOrderRepository.save(any(SalesOrder.class))).thenReturn(salesOrder);
        when(salesOrderMapper.toDto(any(SalesOrder.class))).thenReturn(salesOrderDTO);

        SalesOrderDTO result = salesOrderService.reserveOrderStock(1L, client);

        assertNotNull(result);

        ArgumentCaptor<SalesOrder> orderCaptor = ArgumentCaptor.forClass(SalesOrder.class);
        verify(salesOrderRepository).save(orderCaptor.capture());

        SalesOrder capturedOrder = orderCaptor.getValue();
        assertEquals(SalesOrderStatus.PARTIALLY_RESERVED, capturedOrder.getStatus());
    }

    @Test
    void reserveOrderStock_whenOrderNotFound_shouldThrowException() {
        when(salesOrderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> salesOrderService.reserveOrderStock(999L, client));
    }

    @Test
    void reserveOrderStock_whenUnauthorizedUser_shouldThrowException() {
        User otherUser = new User();
        otherUser.setId(2L);

        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(salesOrder));

        assertThrows(SecurityException.class,
                () -> salesOrderService.reserveOrderStock(1L, otherUser));
    }

    @Test
    void reserveOrderStock_whenStatusNotCreated_shouldThrowException() {
        salesOrder.setStatus(SalesOrderStatus.RESERVED);

        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(salesOrder));

        assertThrows(IllegalStateException.class,
                () -> salesOrderService.reserveOrderStock(1L, client));
    }

    // ========== TESTS getMyOrder ==========

    @Test
    void getMyOrder_shouldReturnUserOrders() {
        SalesOrder order2 = new SalesOrder();
        order2.setId(2L);
        order2.setClient(client);

        SalesOrderDTO dto2 = new SalesOrderDTO();
        dto2.setId(2L);

        when(salesOrderRepository.findAllByClientId(1L))
                .thenReturn(Arrays.asList(salesOrder, order2));
        when(salesOrderMapper.toDto(salesOrder)).thenReturn(salesOrderDTO);
        when(salesOrderMapper.toDto(order2)).thenReturn(dto2);

        List<SalesOrderDTO> result = salesOrderService.getMyOrder(client);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(salesOrderRepository).findAllByClientId(1L);
    }

    @Test
    void getMyOrder_whenNoOrders_shouldReturnEmptyList() {
        when(salesOrderRepository.findAllByClientId(1L))
                .thenReturn(Arrays.asList());

        List<SalesOrderDTO> result = salesOrderService.getMyOrder(client);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ========== TESTS getOrder ==========

    @Test
    void getOrder_whenAuthorized_shouldReturnOrder() {
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(salesOrder));
        when(salesOrderMapper.toDto(salesOrder)).thenReturn(salesOrderDTO);

        SalesOrderDTO result = salesOrderService.getOrder(1L, client);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(salesOrderRepository).findById(1L);
    }

    @Test
    void getOrder_whenNotFound_shouldThrowException() {
        when(salesOrderRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> salesOrderService.getOrder(999L, client));
    }

    @Test
    void getOrder_whenUnauthorized_shouldThrowException() {
        User otherUser = new User();
        otherUser.setId(2L);

        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(salesOrder));

        assertThrows(SecurityException.class,
                () -> salesOrderService.getOrder(1L, otherUser));
    }
}