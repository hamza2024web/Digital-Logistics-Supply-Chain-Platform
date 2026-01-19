package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.request.purchase.PurchaseOrderCreateDTO;
import com.spring.digital_logistics.dto.request.purchase.PurchaseOrderLineCreateDTO;
import com.spring.digital_logistics.dto.response.purchase.PurchaseOrderDTO;
import com.spring.digital_logistics.entity.*;
import com.spring.digital_logistics.entity.enums.PurchaseOrderStatus;
import com.spring.digital_logistics.entity.enums.Role;
import com.spring.digital_logistics.exception.PurchaseOrderStatusException;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.mapper.PurchaseOrderMapper;
import com.spring.digital_logistics.repository.ProductRepository;
import com.spring.digital_logistics.repository.PurchaseOrderRepository;
import com.spring.digital_logistics.repository.SupplierRepository;
import com.spring.digital_logistics.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {

    // --- Mocks pour toutes les dépendances du service ---
    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;
    @Mock
    private SupplierRepository supplierRepository;
    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private PurchaseOrderMapper purchaseOrderMapper;
    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private PurchaseOrderService purchaseOrderService;

    // --- Objets de test réutilisables ---
    private Supplier supplier;
    private Warehouse warehouse;
    private Product product;
    private PurchaseOrder purchaseOrder;
    private User warehouseManager;

    @BeforeEach
    void setUp() {
        // Initialisation des objets de test avant chaque test
        supplier = new Supplier();
        supplier.setId(1L);
        supplier.setName("Fournisseur Test");

        warehouse = new Warehouse();
        warehouse.setId(1L);
        warehouse.setName("Entrepôt Test");

        product = new Product();
        product.setId(1L);
        product.setName("Produit Test");

        purchaseOrder = new PurchaseOrder();
        purchaseOrder.setId(1L);
        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setDestinationWarehouse(warehouse);
        purchaseOrder.setCreationDate(LocalDateTime.now());

        warehouseManager = new User();
        warehouseManager.setRole(Role.WAREHOUSE_MANAGER);
    }

    // === Test pour la méthode createPurchaseOrder ===
    @Test
    void createPurchaseOrder_whenDataIsValid_shouldCreateAndReturnDTO() {
        // Arrange (Préparation)
        PurchaseOrderLineCreateDTO lineDTO = new PurchaseOrderLineCreateDTO(1L, 10, new BigDecimal("99.99"));
        PurchaseOrderCreateDTO createDTO = new PurchaseOrderCreateDTO(1L, 1L, Collections.singletonList(lineDTO));

        // Simuler les retours des dépendances (les mocks)
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder); // Simuler la sauvegarde
        when(purchaseOrderMapper.toDto(any(PurchaseOrder.class))).thenReturn(new PurchaseOrderDTO()); // Simuler le mapping

        // Act (Action)
        PurchaseOrderDTO result = purchaseOrderService.createPurchaseOrder(createDTO);

        // Assert (Vérification)
        assertNotNull(result);
        verify(purchaseOrderRepository).save(any(PurchaseOrder.class)); // Vérifier que la méthode save a bien été appelée
        verify(purchaseOrderMapper).toDto(any(PurchaseOrder.class));    // Vérifier que le mapper a été appelé
    }

    // === Tests pour la méthode sendPurchaseOrder ===
    @Test
    void sendPurchaseOrder_whenStatusIsPending_shouldChangeStatusToSent() {
        // Arrange
        purchaseOrder.setStatus(PurchaseOrderStatus.PENDING);
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
        when(purchaseOrderMapper.toDto(any(PurchaseOrder.class))).thenReturn(new PurchaseOrderDTO());

        // Act
        purchaseOrderService.sendPurchaseOrder(1L);

        // Assert
        assertEquals(PurchaseOrderStatus.SENT, purchaseOrder.getStatus());
        verify(purchaseOrderRepository).save(purchaseOrder); // On vérifie qu'on sauvegarde bien l'objet modifié
    }

    @Test
    void sendPurchaseOrder_whenStatusIsNotPending_shouldThrowException() {
        // Arrange
        purchaseOrder.setStatus(PurchaseOrderStatus.COMPLETED); // Statut incorrect
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));

        // Act & Assert
        // On vérifie qu'une exception est bien lancée
        assertThrows(IllegalStateException.class, () -> {
            purchaseOrderService.sendPurchaseOrder(1L);
        });
        verify(purchaseOrderRepository, never()).save(any()); // On vérifie qu'on n'a JAMAIS appelé la méthode save
    }

    // === Tests pour la méthode receiveOrder ===
    @Test
    void receiveOrder_whenOrderIsSent_shouldUpdateInventoryAndCompleteOrder() {
        // Arrange
        purchaseOrder.setStatus(PurchaseOrderStatus.SENT);
        PurchaseOrderLine line = new PurchaseOrderLine();
        line.setProduct(product);
        line.setQuantity(10);
        line.setPrice(new BigDecimal("12.99"));
        line.setQuantityReceived(0);
        purchaseOrder.setStatus(PurchaseOrderStatus.RECEIVED);
        purchaseOrder.addLine(line);

        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenReturn(purchaseOrder);
        when(purchaseOrderMapper.toDto(any(PurchaseOrder.class))).thenReturn(new PurchaseOrderDTO());
        purchaseOrderService.receiveOrder(1L);

        // Act
        // Assert
        assertEquals(PurchaseOrderStatus.COMPLETED, purchaseOrder.getStatus());
        assertEquals(10, line.getQuantityReceived());
        verify(inventoryService).recordInboundMovement(any()); // Vérifier que l'inventaire a bien été mis à jour
        verify(purchaseOrderRepository).save(purchaseOrder);
    }

    @Test
    void receiveOrder_whenUserIsNotManager_shouldThrowSecurityException() {
        // Arrange
        User clientUser = new User();
        clientUser.setRole(Role.CLIENT); // Mauvais rôle

        // Act & Assert
        // On teste la première garde de sécurité de la méthode
        assertThrows(SecurityException.class, () -> {
            purchaseOrderService.receiveOrder(1L);
        });
        // On vérifie que rien d'autre ne s'est passé
        verify(purchaseOrderRepository, never()).findById(any());
        verify(inventoryService, never()).recordInboundMovement(any());
    }

    @Test
    void receiveOrder_whenOrderStatusIsInvalid_shouldThrowException() {
        // Arrange
        purchaseOrder.setStatus(PurchaseOrderStatus.PENDING); // Statut incorrect
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(purchaseOrder));

        // Act & Assert
        assertThrows(PurchaseOrderStatusException.class, () -> {
            purchaseOrderService.receiveOrder(1L);
        });
    }

    @Test
    void receiveOrder_whenOrderNotFound_shouldThrowResourceNotFoundException() {
        // Arrange
        when(purchaseOrderRepository.findById(anyLong())).thenReturn(Optional.empty()); // Simuler que la commande n'existe pas

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            purchaseOrderService.receiveOrder(999L);
        });
    }
}