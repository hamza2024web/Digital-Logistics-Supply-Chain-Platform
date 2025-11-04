package com.spring.digital_logistics.dto.request.warehouse;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WarehouseCreateDTO {

    @NotBlank(message = "Le code de l'entrepôt ne doit pas étre vide")
    private String code;

    @NotBlank(message = "Le nom de l'entrepôt ne doit pas étre vide")
    private String name;
}
