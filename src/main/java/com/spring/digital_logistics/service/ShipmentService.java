package com.spring.digital_logistics.service;

import com.spring.digital_logistics.dto.request.shipment.ShipmentCreateDTO;
import com.spring.digital_logistics.dto.response.shipment.ShipmentDTO;
import com.spring.digital_logistics.entity.SalesOrder;
import com.spring.digital_logistics.entity.Shipment;
import com.spring.digital_logistics.entity.enums.SalesOrderStatus;
import com.spring.digital_logistics.entity.enums.ShipmentStatus;
import com.spring.digital_logistics.exception.ResourceNotFoundException;
import com.spring.digital_logistics.mapper.ShipmentMapper;
import com.spring.digital_logistics.repository.SalesOrderRepository;
import com.spring.digital_logistics.repository.ShipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final ShipmentMapper shipmentMapper;
    private final InventoryService inventoryService;

    public ShipmentService(ShipmentRepository shipmentRepository, SalesOrderRepository salesOrderRepository, ShipmentMapper shipmentMapper, InventoryService inventoryService) {
        this.shipmentRepository = shipmentRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.shipmentMapper = shipmentMapper;
        this.inventoryService = inventoryService;
    }

    @Transactional
    public ShipmentDTO createAndPlanShipment(Long orderId , ShipmentCreateDTO createDTO){
        SalesOrder order = salesOrderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + orderId));

        if (order.getStatus() != SalesOrderStatus.RESERVED){
            throw new IllegalStateException("Une expédition ne peut être créée que pour une commande avec le statut RESERVED.");
        }

        if (order.getShipment() != null){
            throw new IllegalStateException("Cette commande a déjà une expédition associée.");
        }

        Shipment shipment = new Shipment();
        shipment.setTrackingNumber(createDTO.getTrackingNumber());
        shipment.setStatus(ShipmentStatus.PLANNED);
        shipment.setCreationDate(LocalDateTime.now());
        shipment.setLastUpdatedDate(LocalDateTime.now());

        Shipment savedShipment = shipmentRepository.save(shipment);

        order.setShipment(savedShipment);
        salesOrderRepository.save(order);

        return shipmentMapper.toDto(savedShipment);
    }

    @Transactional
    public ShipmentDTO shipOrder(Long orderId){
        SalesOrder order = salesOrderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Commande non trouvée avec l'ID: " + orderId));

        Shipment shipment = order.getShipment();
        if (shipment == null) {
            throw new IllegalStateException("Cette commande n'a pas d'expédition planifiée.");
        }

        if (order.getStatus() != SalesOrderStatus.RESERVED || shipment.getStatus() != ShipmentStatus.PLANNED) {
            throw new IllegalStateException("L'expédition ne peut être lancée que si la commande est RESERVED et l'expédition PLANNED.");
        }

        inventoryService.recordOutboundMovementForOrder(order);

        order.setStatus(SalesOrderStatus.SHIPPED);
        shipment.setStatus(ShipmentStatus.IN_TRANSIT);
        shipment.setLastUpdatedDate(LocalDateTime.now());

        salesOrderRepository.save(order);
        Shipment updatedShipment = shipmentRepository.save(shipment);

        return shipmentMapper.toDto(updatedShipment);
    }

    @Transactional
    public ShipmentDTO deliverOrder(Long orderId){
        SalesOrder order = salesOrderRepository.findById(orderId).orElseThrow(() -> new ResourceNotFoundException("Commande non trouvé avec L'ID : " + orderId));

        Shipment shipment = order.getShipment();
        if (shipment == null){
            throw new IllegalStateException("Cette commande n'a pas d'expédition associée.");
        }

        if (order.getStatus() != SalesOrderStatus.SHIPPED || shipment.getStatus() != ShipmentStatus.IN_TRANSIT){
            throw new IllegalStateException("Une commande ne peut être marquée comme livrée que si elle est SHIPPED et son expédition IN_TRANSIT.");
        }

        order.setStatus(SalesOrderStatus.DELIVERED);
        shipment.setStatus(ShipmentStatus.DELIVERED);
        shipment.setLastUpdatedDate(LocalDateTime.now());

        salesOrderRepository.save(order);
        Shipment updatedShipment = shipmentRepository.save(shipment);

        return shipmentMapper.toDto(updatedShipment);
    }
}
