package com.spring.digital_logistics.dto.response.inventory;

import com.spring.digital_logistics.entity.enums.MovementType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryMovementDTO {

    private Long productId;
    private String productSku;

    private Long warehouseId;
    private String warehouseCode;

    private MovementType type;
    private int qty;
    private LocalDateTime occurredAt;
}
