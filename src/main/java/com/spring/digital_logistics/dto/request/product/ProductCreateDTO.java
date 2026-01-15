package com.spring.digital_logistics.dto.request.product;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductCreateDTO {
    @NotBlank(message = "Le SKU est requis")
    private String sku;

    @NotBlank(message = "Le nom du produit est requis")
    private String name;

    private String image;  // Optionnel

    @NotNull(message = "Le prix est requis")
    @Min(value = 0, message = "Le prix doit être positif")
    private BigDecimal price;

    private Boolean active = true;
}
