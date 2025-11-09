package com.spring.digital_logistics.dto.response.salesOrder;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesOrderLineDTO {
    private Long id;
    private Long productId;
    private String productSku;
    private int quantity;
    private BigDecimal price;
}
