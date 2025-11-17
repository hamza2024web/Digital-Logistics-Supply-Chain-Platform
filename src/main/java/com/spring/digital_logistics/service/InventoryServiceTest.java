package com.spring.digital_logistics.service;

import com.spring.digital_logistics.entity.*;
import com.spring.digital_logistics.entity.enums.SalesOrderLineStatus;
import com.spring.digital_logistics.entity.enums.SalesOrderStatus;
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
        line.setQuantity(10);
        order.addLine(line);

        Inventory inventory = new Inventory(product,warehouse,8,3);
        when(inventoryRepository.findByProductAndWarehouse(product,warehouse)).thenReturn(Optional.of(inventory));

        boolean result = inventoryService.reserveStockForOrder(order);

        assertFalse(result, "Le résultat devrait être 'false' car le stock est insuffisant.");

        assertEquals(SalesOrderLineStatus.BACKORDERED, line.getStatus(),"Le statut de la ligne devrait être BACKORDERED.");

        verify(inventoryRepository, never()).save(any(Inventory.class));
    }

    @Test
    void reserveStockForOrder_withMultipleLines_andMixedStock_shouldPartiallyReserveAndReturnFalse(){
        Product productB_Jeans = new Product();
        productB_Jeans.setId(2L);
        productB_Jeans.setSku("JEANS_NOIR");

        SalesOrder order = new SalesOrder();
        order.setWarehouse(warehouse);

        SalesOrderLine lineA_Tshirt = new SalesOrderLine();
        lineA_Tshirt.setProduct(product);
        lineA_Tshirt.setQuantity(5);
        order.addLine(lineA_Tshirt);

        SalesOrderLine lineB_Jeans = new SalesOrderLine();
        lineB_Jeans.setProduct(productB_Jeans);
        lineB_Jeans.setQuantity(10);
        order.addLine(lineB_Jeans);

        Inventory inventoryA = new Inventory(product , warehouse , 10 , 0);
        when(inventoryRepository.findByProductAndWarehouse(product,warehouse)).thenReturn(Optional.of(inventoryA));

        Inventory inventoryB = new Inventory(productB_Jeans, warehouse, 5, 3);
        when(inventoryRepository.findByProductAndWarehouse(productB_Jeans,warehouse)).thenReturn(Optional.of(inventoryB));

        boolean result = inventoryService.reserveStockForOrder(order);

        assertFalse(result, "Le résultat devrait être 'false' car le stock est partiellement insuffisant.");

        assertEquals(SalesOrderLineStatus.RESERVED,lineA_Tshirt.getStatus(),"La ligne A (T-shirt) devrait être RESERVED.);");

        assertEquals(SalesOrderLineStatus.BACKORDERED,lineB_Jeans.getStatus(), "La ligne B (Jean) devrait être BACKORDERED.");

        ArgumentCaptor<Inventory> inventoryCaptor = ArgumentCaptor.forClass(Inventory.class);
        verify(inventoryRepository, times(1)).save(inventoryCaptor.capture());

        Inventory savedInventory = inventoryCaptor.getValue();
        assertEquals("TSHIRT-BLEU", savedInventory.getProduct().getSku(), "C'est l'inventaire du T-shirt qui aurait dû être sauvegardé.");
        assertEquals(5, savedInventory.getQtyReserved(),"La quantité réservée pour le T-shirt devrait être 5.");
    }
}
