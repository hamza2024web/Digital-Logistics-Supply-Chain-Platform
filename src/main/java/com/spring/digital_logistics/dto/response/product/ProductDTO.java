package com.spring.digital_logistics.dto.response.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Long id;
    private String sku;
    private String name;
    private String image;
    private BigDecimal price;
    private boolean active;
}
