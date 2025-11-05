package com.spring.digital_logistics.dto.response.purchase;

import lombok.Data;

@Data
public class PurchaseOrderLineDTO {
    private Long id;
    private Long productId;
    private String productSku;
    private Integer quantity;
}
