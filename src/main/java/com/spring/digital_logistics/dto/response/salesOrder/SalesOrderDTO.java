package com.spring.digital_logistics.dto.response.salesOrder;

import com.spring.digital_logistics.entity.enums.SalesOrderStatus;
import com.spring.digital_logistics.entity.enums.ShipmentStatus;

import java.time.LocalDateTime;

public class SalesOrderDTO {
    private Long id;
    private Long clientId;
    private String clientName;
    private Long warehouseId;
    private String warehouseCode;
    private SalesOrderStatus status;
    private LocalDateTime createdAt;
    private String shipementCarrier;
    private String shipementTrackingNumber;
    private ShipmentStatus shipmentStatus;
}
