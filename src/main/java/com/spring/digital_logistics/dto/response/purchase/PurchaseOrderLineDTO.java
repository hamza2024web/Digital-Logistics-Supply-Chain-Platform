package com.spring.digital_logistics.dto.response.purchase;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PurchaseOrderLineDTO {
    private Long id;
    private Long productId;
    private String productSku;
    private Integer quantityOrdered;
    private Integer quantityReceived;
    private BigDecimal price;
}
