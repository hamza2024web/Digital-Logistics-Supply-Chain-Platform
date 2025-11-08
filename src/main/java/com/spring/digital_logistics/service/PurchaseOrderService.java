package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.request.purchase.PurchaseOrderCreateDTO;
import com.spring.digital_logistics.dto.request.purchase.PurchaseOrderLineCreateDTO;
import com.spring.digital_logistics.dto.response.purchase.PurchaseOrderDTO;
import com.spring.digital_logistics.entity.*;
import com.spring.digital_logistics.entity.enums.PurchaseOrderStatus;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.mapper.PurchaseOrderMapper;
import com.spring.digital_logistics.repository.ProductRepository;
import com.spring.digital_logistics.repository.PurchaseOrderRepository;
import com.spring.digital_logistics.repository.SupplierRepository;
import com.spring.digital_logistics.repository.WarehouseRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class PurchaseOrderService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;

    public PurchaseOrderService(PurchaseOrderRepository purchaseOrderRepository, SupplierRepository supplierRepository, WarehouseRepository warehouseRepository, ProductRepository productRepository, PurchaseOrderMapper purchaseOrderMapper){
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.supplierRepository = supplierRepository;
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.purchaseOrderMapper = purchaseOrderMapper;
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
}
