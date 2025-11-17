package com.spring.digital_logistics.service;

import com.spring.digital_logistics.entity.Product;
import com.spring.digital_logistics.entity.TransferOrder;
import com.spring.digital_logistics.entity.Warehouse;
import com.spring.digital_logistics.entity.enums.TransferStatus;
import com.spring.digital_logistics.repository.TransferOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferExecutionServiceTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private TransferOrderRepository transferOrderRepository;

    @InjectMocks
    private TransferExecutionService transferExecutionService;

    private Product product;
    private Warehouse sourceWarehouse;
    private Warehouse destinationWarehouse;
    private TransferOrder transferOrder;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setSku("SKU-001");

        sourceWarehouse = new Warehouse();
        sourceWarehouse.setId(1L);
        sourceWarehouse.setCode("W-SOURCE");

        destinationWarehouse = new Warehouse();
        destinationWarehouse.setId(2L);
        destinationWarehouse.setCode("W-DEST");

        transferOrder = new TransferOrder();
        transferOrder.setId(1L);
        transferOrder.setProduct(product);
        transferOrder.setSourceWarehouse(sourceWarehouse);
        transferOrder.setDestinationWarehouse(destinationWarehouse);
        transferOrder.setQuantity(10);
        transferOrder.setStatus(TransferStatus.PENDING);
    }

    // ========== TESTS executePendingTransfers ==========

    @Test
    void executePendingTransfers_withPendingOrders_shouldExecuteTransfers() {
        when(transferOrderRepository.findByStatus(TransferStatus.PENDING))
                .thenReturn(List.of(transferOrder));
        doNothing().when(inventoryService).shipStock(product, sourceWarehouse, 10);
        doNothing().when(inventoryService).receiveStock(product, destinationWarehouse, 10);
        when(transferOrderRepository.save(transferOrder)).thenReturn(transferOrder);

        transferExecutionService.executePendingTransfers();

        verify(inventoryService).shipStock(product, sourceWarehouse, 10);
        verify(inventoryService).receiveStock(product, destinationWarehouse, 10);

        ArgumentCaptor<TransferOrder> captor = ArgumentCaptor.forClass(TransferOrder.class);
        verify(transferOrderRepository).save(captor.capture());

        TransferOrder savedOrder = captor.getValue();
        assertEquals(TransferStatus.COMPLETED, savedOrder.getStatus());
    }

    @Test
    void executePendingTransfers_withMultiplePendingOrders_shouldExecuteAll() {
        TransferOrder transfer2 = new TransferOrder();
        transfer2.setId(2L);
        transfer2.setProduct(product);
        transfer2.setSourceWarehouse(sourceWarehouse);
        transfer2.setDestinationWarehouse(destinationWarehouse);
        transfer2.setQuantity(5);
        transfer2.setStatus(TransferStatus.PENDING);

        when(transferOrderRepository.findByStatus(TransferStatus.PENDING))
                .thenReturn(List.of(transferOrder, transfer2));
        doNothing().when(inventoryService).shipStock(any(), any(), anyInt());
        doNothing().when(inventoryService).receiveStock(any(), any(), anyInt());
        when(transferOrderRepository.save(any())).thenReturn(transferOrder);

        transferExecutionService.executePendingTransfers();

        verify(inventoryService, times(2)).shipStock(any(), any(), anyInt());
        verify(inventoryService, times(2)).receiveStock(any(), any(), anyInt());
        verify(transferOrderRepository, times(2)).save(any());
    }

    @Test
    void executePendingTransfers_withNoPendingOrders_shouldDoNothing() {
        when(transferOrderRepository.findByStatus(TransferStatus.PENDING))
                .thenReturn(List.of());

        transferExecutionService.executePendingTransfers();

        verify(inventoryService, never()).shipStock(any(), any(), anyInt());
        verify(inventoryService, never()).receiveStock(any(), any(), anyInt());
        verify(transferOrderRepository, never()).save(any());
    }

    @Test
    void executePendingTransfers_shouldUseCorrectQuantities() {
        transferOrder.setQuantity(15);

        when(transferOrderRepository.findByStatus(TransferStatus.PENDING))
                .thenReturn(List.of(transferOrder));
        doNothing().when(inventoryService).shipStock(product, sourceWarehouse, 15);
        doNothing().when(inventoryService).receiveStock(product, destinationWarehouse, 15);
        when(transferOrderRepository.save(transferOrder)).thenReturn(transferOrder);

        transferExecutionService.executePendingTransfers();

        verify(inventoryService).shipStock(product, sourceWarehouse, 15);
        verify(inventoryService).receiveStock(product, destinationWarehouse, 15);
    }
}