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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SalesOrderService {

    private final SalesOrderRepository salesOrderRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final SalesOrderMapper salesOrderMapper;
    private final InventoryService inventoryService;

    public SalesOrderService(SalesOrderRepository salesOrderRepository, ProductRepository productRepository, WarehouseRepository warehouseRepository, SalesOrderMapper salesOrderMapper, InventoryService inventoryService) {
        this.salesOrderRepository = salesOrderRepository;
        this.productRepository = productRepository;
        this.warehouseRepository = warehouseRepository;
        this.salesOrderMapper = salesOrderMapper;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public SalesOrderDTO createOrder(SalesOrderCreateDTO createDTO , User client){
        Warehouse warehouse = warehouseRepository.findById(createDTO.getWarehouseId())
                .orElseThrow(() -> new ResourceNotFoundException("Entrepôt non trouvé avec l'ID: " + createDTO.getWarehouseId()));

        SalesOrder salesOrder = new SalesOrder();
        salesOrder.setClient(client);
        salesOrder.setWarehouse(warehouse);
        salesOrder.setStatus(SalesOrderStatus.CREATED);
        salesOrder.setCreatedAt(LocalDateTime.now());

        for (SalesOrderLineCreateDTO lineDTO : createDTO.getLines()){

            Product product = productRepository.findById(lineDTO.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Produit non trouvé avec l'ID: " + lineDTO.getProductId()));

            SalesOrderLine line = new SalesOrderLine();
            line.setProduct(product);
            line.setQuantity(lineDTO.getQuantity());

            salesOrder.addLine(line);
        }

        SalesOrder savedOrder = salesOrderRepository.save(salesOrder);

        return salesOrderMapper.toDto(savedOrder);
    }

    @Transactional
    public SalesOrderDTO reserveOrderStock(Long orderId , User client){
        SalesOrder order = salesOrderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Commande non trouvé avec l'ID : " + orderId));

        if (!order.getClient().getId().equals(client.getId())){
            throw new SecurityException("Vous n'êtes pas autorisé à modifier cette commande.");
        }

        if (order.getStatus() != SalesOrderStatus.CREATED){
            throw new IllegalStateException("Seule une commande avec le statut CREATED peut être réservée. Statut actuel: " + order.getStatus());
        }

        inventoryService.reserveStockForOrder(order);

        order.setStatus(SalesOrderStatus.RESERVED);
        SalesOrder savedOrder = salesOrderRepository.save(order);

        return salesOrderMapper.toDto(savedOrder);
    }

    public List<SalesOrderDTO> getMyOrder(User client){
        List<SalesOrder> orders = salesOrderRepository.findAllByClientId(client.getId());
        return orders.stream().map(salesOrderMapper::toDto).collect(Collectors.toList());
    }

    public SalesOrderDTO getOrder(Long orderId , User client){
        SalesOrder order = salesOrderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Commande non trouvé avec l'ID : " + orderId));

        if (!order.getClient().getId().equals(client.getId())){
            throw new SecurityException("Vous n'êtes pas autorisé à voir cette commande.");
        }

        return salesOrderMapper.toDto(order);
    }
}
