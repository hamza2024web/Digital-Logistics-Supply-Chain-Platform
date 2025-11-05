package com.spring.digital_logistics.dto.response.purchase;

import com.spring.digital_logistics.entity.enums.POStatus;

import java.time.LocalDateTime;
import java.util.List;

public class PurchaseOrderDTO {
    private Long id;
    private Long supplierId;
    private String supplierName;
    private Long destinationWarehouseId;
    private String destinationWarehouseCode;
    private POStatus status;
    private LocalDateTime createdAt;
    private List<PurchaseOrderLineDTO> lines;
}
