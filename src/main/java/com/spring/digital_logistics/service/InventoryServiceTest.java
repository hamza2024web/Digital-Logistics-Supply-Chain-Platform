package com.spring.digital_logistics.service;

import com.spring.digital_logistics.entity.*;
import com.spring.digital_logistics.entity.enums.SalesOrderLineStatus;
import com.spring.digital_logistics.repository.InventoryMovementRepository;
import com.spring.digital_logistics.repository.InventoryRepository;
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
    private InventoryRepository inventoryRepository;
    @Mock
    private InventoryMovementRepository inventoryMovementRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Product product;
    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(1L);
        product.setSku("TSHIRT-BLEU");

        warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setCode("W-PARIS");
    }

    @Test
    void reserveStockForOrder_whenStockIsSufficient_shouldReserveStockAndReturnTrue(){
        SalesOrder order = new SalesOrder();
        order.setWarehouse(warehouse);
        SalesOrderLine line = new SalesOrderLine();
        line.setProduct(product);
        line.setQuantity(5);
        order.addLine(line);

        Inventory inventory = new Inventory(product,warehouse,10,0);
        when(inventoryRepository.findByProductAndWarehouse(product,warehouse)).thenReturn(Optional.of(inventory));

        boolean result = inventoryService.reserveStockForOrder(order);

        assertTrue(result);

        assertEquals(SalesOrderLineStatus.RESERVED, line.getStatus());

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository).save(inventoryCaptor.capture());
        Inventory savedInventory = inventoryCaptor.getValue();

        assertEquals(5, savedInventory.getQtyReserved());
        assertEquals(10, savedInventory.getQtyOnHand());
    }

    @Test
    void reserveStockForOrder_whenStockIsInsufficient_shouldBackorderAndReturnFalse(){
        SalesOrder order = new SalesOrder();
        order.setWarehouse(warehouse);
        SalesOrderLine line = new SalesOrderLine();
        line.setProduct(product);
        line.setQuantity(20);
        order.addLine(line);

        Inventory inventory = new Inventory(product,warehouse,8,3);
        when(inventoryRepository.findByProductAndWarehouse(product,warehouse)).thenReturn(Optional.of(inventory));

        boolean result = inventoryService.reserveStockForOrder(order);

        assertFalse(result, "Le résultat devrait être 'false' car le stock est insuffisant.");

        assertEquals(SalesOrderLineStatus.BACKORDERED, line.getStatus(),"Le statut de la ligne devrait être BACKORDERED.");

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }
}
