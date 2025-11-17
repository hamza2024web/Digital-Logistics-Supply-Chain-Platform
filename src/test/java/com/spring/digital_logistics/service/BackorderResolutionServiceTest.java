package com.spring.digital_logistics.service;

import com.spring.digital_logistics.entity.*;
import com.spring.digital_logistics.entity.enums.SalesOrderLineStatus;
import com.spring.digital_logistics.entity.enums.SalesOrderStatus;
import com.spring.digital_logistics.entity.enums.TransferStatus;
import com.spring.digital_logistics.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class BackorderResolutionServiceTest {

    @Mock private SalesOrderLineRepository salesOrderLineRepository;
    @Mock private InventoryRepository inventoryRepository;
    @Mock private WarehouseRepository warehouseRepository;
    @Mock private TransferOrderRepository transferOrderRepository;
    @Mock private SalesOrderRepository salesOrderRepository;
    @InjectMocks private BackorderResolutionService service;

    @BeforeEach void setUp() { MockitoAnnotations.openMocks(this); }

    // Test la résolution des lignes en backorder
    @Test
    void resolveBackorderLines_shouldCreateTransferOrderAndChangeStatus() {
        Product product = new Product();
        product.setId(1L);
        product.setSku("PROD-1");
        Warehouse destWh = new Warehouse();
        destWh.setId(1L);
        destWh.setCode("W-DEST");
        Warehouse srcWh = new Warehouse();
        srcWh.setId(2L);
        srcWh.setCode("W-SRC");
        SalesOrder order = new SalesOrder();
        order.setId(1L);
        order.setWarehouse(destWh);
        SalesOrderLine line = new SalesOrderLine();
        line.setProduct(product);
        line.setQuantity(5);
        line.setStatus(SalesOrderLineStatus.BACKORDERED);
        line.setSalesOrder(order);

        List<SalesOrderLine> backorderedLines = Collections.singletonList(line);
        List<Warehouse> warehouses = Arrays.asList(destWh, srcWh);
        Inventory remoteInventory = new Inventory(product, srcWh, 10, 0);

        when(salesOrderLineRepository.findByStatus(SalesOrderLineStatus.BACKORDERED)).thenReturn(backorderedLines);
        when(warehouseRepository.findAll()).thenReturn(warehouses);
        when(inventoryRepository.findByProductAndWarehouse(product, srcWh)).thenReturn(Optional.of(remoteInventory));

        service.resolveBackordersAndAwaitingTransfers();

        verify(transferOrderRepository).save(any(TransferOrder.class));
        verify(salesOrderLineRepository).save(argThat(savedLine ->
                savedLine.getStatus() == SalesOrderLineStatus.AWAITING_TRANSFER)
        );
    }

    // Test la résolution des lignes en awaiting_transfer
    @Test
    void resolveAwaitingTransferLines_shouldReserveStockAndChangeStatus() {
        Product product = new Product();
        product.setId(1L);
        product.setSku("PROD-2");
        Warehouse warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setCode("W-DEST2");
        SalesOrder order = new SalesOrder();
        order.setId(2L);
        order.setWarehouse(warehouse);

        SalesOrderLine line = new SalesOrderLine();
        line.setProduct(product);
        line.setQuantity(5);
        line.setStatus(SalesOrderLineStatus.AWAITING_TRANSFER);
        line.setSalesOrder(order);

        List<SalesOrderLine> awaitingLines = Collections.singletonList(line);
        Inventory inventory = new Inventory(product, warehouse, 10, 0);

        when(salesOrderLineRepository.findByStatus(SalesOrderLineStatus.AWAITING_TRANSFER)).thenReturn(awaitingLines);
        when(inventoryRepository.findByProductAndWarehouse(product, warehouse)).thenReturn(Optional.of(inventory));
        when(salesOrderRepository.findById(order.getId())).thenReturn(Optional.of(order));

        service.resolveBackordersAndAwaitingTransfers();

        verify(inventoryRepository).save(argThat(inv -> inv.getQtyReserved() == 5));
        verify(salesOrderLineRepository).save(argThat(savedLine ->
                savedLine.getStatus() == SalesOrderLineStatus.RESERVED)
        );
        verify(salesOrderRepository).save(order); // Mise à jour du statut de la commande si toutes les lignes sont reservées
    }

    // Les branches "pas de lignes à traiter"
    @Test
    void resolveBackordersAndAwaitingTransfers_whenNoLines_shouldNotFailOrSave() {
        when(salesOrderLineRepository.findByStatus(SalesOrderLineStatus.AWAITING_TRANSFER)).thenReturn(Collections.emptyList());
        when(salesOrderLineRepository.findByStatus(SalesOrderLineStatus.BACKORDERED)).thenReturn(Collections.emptyList());

        service.resolveBackordersAndAwaitingTransfers();

        verifyNoInteractions(transferOrderRepository, inventoryRepository, warehouseRepository, salesOrderRepository);
    }
}