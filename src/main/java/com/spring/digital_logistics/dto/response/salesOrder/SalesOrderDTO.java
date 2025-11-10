package com.spring.digital_logistics.dto.response.salesOrder;

import com.spring.digital_logistics.dto.response.shipment.ShipmentDTO;
import com.spring.digital_logistics.entity.enums.SalesOrderStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SalesOrderDTO {
    private Long id;
    private Long clientId;
    private String clientUsername;
    private Long warehouseId;
    private String warehouseCode;
    private SalesOrderStatus status;
    private LocalDateTime createdAt;
    private List<SalesOrderLineDTO> lines;

    private ShipmentDTO shipment;
}
