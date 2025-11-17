package com.spring.digital_logistics.service;

import com.spring.digital_logistics.IntegrationTestBase;
import com.spring.digital_logistics.dto.request.purchase.PurchaseOrderCreateDTO;
import com.spring.digital_logistics.dto.request.purchase.PurchaseOrderLineCreateDTO;
import com.spring.digital_logistics.entity.*;
import com.spring.digital_logistics.entity.enums.PurchaseOrderStatus;
import com.spring.digital_logistics.entity.enums.Role;
import com.spring.digital_logistics.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class PurchaseOrderServiceIntegrationTest extends IntegrationTestBase {

    @Autowired
    private PurchaseOrderService purchaseOrderService;

    @Autowired
    private PurchaseOrderRepository purchaseOrderRepository;
    @Autowired
    private SupplierRepository supplierRepository;
    @Autowired
    private WarehouseRepository warehouseRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Autowired
    private UserRepository userRepository;

    private Supplier savedSupplier;
    private Warehouse savedWarehouse;
    private Product savedProduct;
    private User savedWarehouseManager;

    @BeforeEach
    void setupDatabase() {
        savedSupplier = supplierRepository.save(new Supplier("M. Dupont", "contact@fournisseur.com", "contact@fournisseur.com", "0678524196"));
        savedWarehouse = warehouseRepository.save(new Warehouse("W-INT-PO", "Entrepôt Test Commande"));
        savedProduct = productRepository.save(new Product("PROD-INT-PO", "Produit Test Commande", "null", BigDecimal.TEN, true));

        User manager = new User("manger1","test","manager@test.com", "password", Role.WAREHOUSE_MANAGER);
        savedWarehouseManager = userRepository.save(manager);
    }

    @Test
    void createPurchaseOrder_shouldCreateAndSaveOrderCorrectlyInDatabase() {
        // Arrange (Préparation)
        PurchaseOrderLineCreateDTO lineDto = new PurchaseOrderLineCreateDTO(savedProduct.getId(), 50, new BigDecimal("12.50"));
        PurchaseOrderCreateDTO createDto = new PurchaseOrderCreateDTO(savedSupplier.getId(), savedWarehouse.getId(), Collections.singletonList(lineDto));

        // Act (Action)
        var createdOrderDto = purchaseOrderService.createPurchaseOrder(createDto);

        // Assert (Vérification)
        assertNotNull(createdOrderDto.getId());

        // On vérifie directement dans la base de données
        PurchaseOrder foundOrder = purchaseOrderRepository.findById(createdOrderDto.getId()).orElseThrow();

        assertEquals(PurchaseOrderStatus.PENDING, foundOrder.getStatus());
        assertEquals(savedSupplier.getId(), foundOrder.getSupplier().getId());
        assertEquals(1, foundOrder.getLines().size());
        assertEquals(savedProduct.getId(), foundOrder.getLines().get(0).getProduct().getId());
        assertEquals(50, foundOrder.getLines().get(0).getQuantity());
    }

    @Test
    void sendPurchaseOrder_whenStatusIsPending_shouldUpdateStatusToSentInDatabase() {
        // Arrange
        // On crée une commande directement dans la BDD pour ce test
        PurchaseOrder order = new PurchaseOrder();
        order.setSupplier(savedSupplier);
        order.setDestinationWarehouse(savedWarehouse);
        order.setStatus(PurchaseOrderStatus.PENDING);
        PurchaseOrder savedOrder = purchaseOrderRepository.save(order);

        // Act
        purchaseOrderService.sendPurchaseOrder(savedOrder.getId());

        // Assert
        PurchaseOrder updatedOrder = purchaseOrderRepository.findById(savedOrder.getId()).orElseThrow();
        assertEquals(PurchaseOrderStatus.SENT, updatedOrder.getStatus());
    }

    @Test
    void receiveOrder_whenOrderIsReceived_shouldUpdateInventoryAndCompleteOrderStatus() {
        // Arrange
        // On crée une commande avec le statut RECEIVED
        PurchaseOrder order = new PurchaseOrder();
        order.setSupplier(savedSupplier);
        order.setDestinationWarehouse(savedWarehouse);
        order.setStatus(PurchaseOrderStatus.RECEIVED); // Important : le statut doit être RECEIVED

        PurchaseOrderLine line = new PurchaseOrderLine();
        line.setProduct(savedProduct);
        line.setQuantity(100);
        order.addLine(line);
        PurchaseOrder savedOrder = purchaseOrderRepository.save(order);

        // Act
        purchaseOrderService.receiveOrder(savedOrder.getId(), savedWarehouseManager);

        // Assert
        // 1. Vérifier que le statut de la commande est bien COMPLETED
        PurchaseOrder updatedOrder = purchaseOrderRepository.findById(savedOrder.getId()).orElseThrow();
        assertEquals(PurchaseOrderStatus.COMPLETED, updatedOrder.getStatus());
        assertEquals(100, updatedOrder.getLines().get(0).getQuantityReceived());

        // 2. Vérifier que l'inventaire a bien été mis à jour
        Inventory inventory = inventoryRepository.findByProductAndWarehouse(savedProduct, savedWarehouse).orElseThrow();
        assertEquals(100, inventory.getQtyOnHand(), "La quantité en stock devrait être de 100 après réception.");
    }
}