package com.spring.digital_logistics.dto.response.shipment;

import com.spring.digital_logistics.entity.enums.ShipmentStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ShipmentDTO {
    private Long id;
    private String trackingNumber;
    private ShipmentStatus status;
    private LocalDateTime creationDate;
    private LocalDateTime lastUpdatedDate;
}
