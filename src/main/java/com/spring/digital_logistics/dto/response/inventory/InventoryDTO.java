package com.spring.digital_logistics.dto.response.inventory;

import lombok.Data;

@Data
public class InventoryDTO {
    private Long id;
    private Long productId;
    private String productSku;
    private Long warehouseId;
    private String warehouseCode;
    private int qtyOnHand;
    private int qtyReserved;
}
