package com.spring.digital_logistics.service;

import com.spring.digital_logistics.entity.PurchaseOrder;
import com.spring.digital_logistics.entity.PurchaseOrderLine;
import com.spring.digital_logistics.entity.Product;
import com.spring.digital_logistics.entity.Warehouse;
import com.spring.digital_logistics.entity.enums.PurchaseOrderStatus;
import com.spring.digital_logistics.repository.PurchaseOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SupplierSimulationServiceTest {
    @Mock private PurchaseOrderRepository purchaseOrderRepository;
    @InjectMocks private SupplierSimulationService supplierSimulationService;

    @BeforeEach void setUp() { MockitoAnnotations.openMocks(this); }

    @Test
    void simulateSupplierDeliveries_shouldMarkOrdersAsReceived() {
        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setCode("W-1");
        Product product = new Product();
        product.setId(2L);
        product.setSku("SKU-200");
        PurchaseOrderLine line = new PurchaseOrderLine();
        line.setProduct(product);
        line.setQuantity(10);
        PurchaseOrder order = new PurchaseOrder();
        order.setId(99L);
        order.setDestinationWarehouse(warehouse);
        order.setStatus(PurchaseOrderStatus.SENT);
        order.setLines(List.of(line));
        line.setPurchaseOrder(order);

        when(purchaseOrderRepository.findByStatus(PurchaseOrderStatus.SENT)).thenReturn(List.of(order));
        when(purchaseOrderRepository.save(any())).thenReturn(order);

        supplierSimulationService.simulateSupplierDeliveries();

        assertEquals(PurchaseOrderStatus.RECEIVED, order.getStatus());
        verify(purchaseOrderRepository).save(order);
    }

    @Test
    void simulateSupplierDeliveries_whenNoSentOrders_shouldReturnGracefully() {
        when(purchaseOrderRepository.findByStatus(PurchaseOrderStatus.SENT)).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> supplierSimulationService.simulateSupplierDeliveries());
        verify(purchaseOrderRepository, never()).save(any());
    }
}