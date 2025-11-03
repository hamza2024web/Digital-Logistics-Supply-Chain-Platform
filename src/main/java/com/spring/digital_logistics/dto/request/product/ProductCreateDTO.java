package com.spring.digital_logistics.dto.request.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductCreateDTO {
    @NotBlank(message = "Le Sku ne doit pas étre vide")
    private String sku;

    @NotBlank(message = "Le nom ne doit pas étre vide")
    private String name;

    private String image;
    private BigDecimal price;
    private String unit;

    @NotNull(message = "Le Statut actif ne doit pas étre vide")
    private boolean active;
}
