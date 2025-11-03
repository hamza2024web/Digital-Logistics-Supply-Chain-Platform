package com.spring.digital_logistics.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

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
