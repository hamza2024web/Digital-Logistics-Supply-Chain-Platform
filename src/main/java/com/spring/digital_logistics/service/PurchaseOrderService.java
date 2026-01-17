package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.request.inventory.MovementRequestDTO;
import com.spring.digital_logistics.dto.request.purchase.PurchaseOrderCreateDTO;
import com.spring.digital_logistics.dto.request.purchase.PurchaseOrderLineCreateDTO;
import com.spring.digital_logistics.dto.response.purchase.PurchaseOrderDTO;
import com.spring.digital_logistics.entity.*;
import com.spring.digital_logistics.entity.enums.PurchaseOrderStatus;
import com.spring.digital_logistics.entity.enums.Role;
import com.spring.digital_logistics.exception.BusinessException;
import com.spring.digital_logistics.exception.PurchaseOrderStatusException;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.mapper.PurchaseOrderMapper;
import com.spring.digital_logistics.repository.ProductRepository;
import com.spring.digital_logistics.repository.PurchaseOrderRepository;
import com.spring.digital_logistics.repository.SupplierRepository;
import com.spring.digital_logistics.repository.WarehouseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final InventoryService inventoryService;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository, SupplierRepository supplierRepository, WarehouseRepository warehouseRepository, ProductRepository productRepository, PurchaseOrderMapper purchaseOrderMapper, InventoryService inventoryService){
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.purchaseOrderMapper = purchaseOrderMapper;
        this.inventoryService = inventoryService;
    }

    public List<PurchaseOrderDTO> getAllPurchaseOrders(){
        return purchaseOrderRepository.findAll().stream().map(purchaseOrderMapper::toDto).toList();
    }

    public PurchaseOrderDTO getPurchaseOrderById(Long id){
        PurchaseOrder order = purchaseOrderRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Order non trouvé avec l'ID : " + id));
        return purchaseOrderMapper.toDto(order);
    }

    public PurchaseOrderDTO createPurchaseOrder(PurchaseOrderCreateDTO createDTO){
        Supplier supplier = supplierRepository.findById(createDTO.getSupplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Fournisseur non trouvé avec l'ID: " + createDTO.getSupplierId()));

        Warehouse warehouse = warehouseRepository.findById(createDTO.getDestinationWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt non trouvé avec l'ID: " + createDTO.getDestinationWarehouseId()));

        PurchaseOrder purchaseOrder = new PurchaseOrder();
        purchaseOrder.setSupplier(supplier);
        purchaseOrder.setDestinationWarehouse(warehouse);
        purchaseOrder.setStatus(PurchaseOrderStatus.PENDING);
        purchaseOrder.setCreationDate(LocalDateTime.now());

        for(PurchaseOrderLineCreateDTO lineDTO : createDTO.getLines()){
            Product product = productRepository.findById(lineDTO.getProductId()).orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + lineDTO.getProductId()));

            PurchaseOrderLine line = new PurchaseOrderLine();
            line.setProduct(product);
            line.setQuantity(lineDTO.getQuantity());
            line.setPrice(lineDTO.getPrice());
            line.setQuantityReceived(0);

            purchaseOrder.addLine(line);
        }

        PurchaseOrder savedOrder = purchaseOrderRepository.save(purchaseOrder);

        return purchaseOrderMapper.toDto(savedOrder);
    }

    @Transactional
    public PurchaseOrderDTO sendPurchaseOrder(Long orderId){
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + orderId));

        if (purchaseOrder.getStatus() != PurchaseOrderStatus.PENDING){
            throw new IllegalStateException("Seule une commande avec le statut PENDING peut être envoyée. Statut actuel : " + purchaseOrder.getStatus());
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.SENT);

        PurchaseOrder savedPurchase = purchaseOrderRepository.save(purchaseOrder);
        return purchaseOrderMapper.toDto(savedPurchase);
    }

    @Transactional
    public PurchaseOrderDTO receiveOrder(Long purchaseOrderId , User warehouseUser){

        if (warehouseUser.getRole() != Role.WAREHOUSE_MANAGER){
            throw new SecurityException("Vous n'étes pas autorisé de faire cette action");
        }

        PurchaseOrder order = purchaseOrderRepository.findById(purchaseOrderId).orElseThrow(() -> new ResourceNotFoundException("Purchase Order non trouvé avec L'ID : " + purchaseOrderId));

        if (order.getStatus() != PurchaseOrderStatus.RECEIVED){
            throw new PurchaseOrderStatusException("Cette commande n'est pas en attente de réception. Statut actuel: " + order.getStatus());
        }

        for (PurchaseOrderLine line : order.getLines()) {
            int quantityAlreadyReceived = line.getQuantityReceived();
            int quantityOrdered = line.getQuantity();

            if (quantityAlreadyReceived < quantityOrdered) {
                int quantityToReceiveNow = quantityOrdered - quantityAlreadyReceived;

                MovementRequestDTO inboundInstruction = new MovementRequestDTO();
                inboundInstruction.setProductId(line.getProduct().getId());
                inboundInstruction.setWarehouseId(order.getDestinationWarehouse().getId());
                inboundInstruction.setQuantity(quantityToReceiveNow);

                inventoryService.recordInboundMovement(inboundInstruction);

                line.setQuantityReceived(quantityOrdered);
            }
        }

        boolean isFullyCompleted = order.getLines().stream()
                .allMatch(line -> line.getQuantityReceived() >= line.getQuantity());

        if (isFullyCompleted) {
            order.setStatus(PurchaseOrderStatus.COMPLETED);
        } else {
            order.setStatus(PurchaseOrderStatus.PARTIALLY_RECEIVED);
        }

        PurchaseOrder savedOrder = purchaseOrderRepository.save(order);
        return purchaseOrderMapper.toDto(savedOrder);
    }

    @Transactional
    public PurchaseOrderDTO cancelPurchaseOrder(Long id) {
        PurchaseOrder order = purchaseOrderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order non trouvée avec l'ID : " + id));

        if (order.getStatus() == PurchaseOrderStatus.RECEIVED
                || order.getStatus() == PurchaseOrderStatus.COMPLETED
                || order.getStatus() == PurchaseOrderStatus.PARTIALLY_RECEIVED
                || order.getStatus() == PurchaseOrderStatus.CANCELLED) {
            throw new BusinessException("Impossible d'annuler une commande déjà reçue ou partiellement reçue ou déjà annulé");
        }

        order.setStatus(PurchaseOrderStatus.CANCELLED);
        purchaseOrderRepository.save(order);

        return purchaseOrderMapper.toDto(order);
    }

}
