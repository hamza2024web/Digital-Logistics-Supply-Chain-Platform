package com.spring.digital_logistics.dto.request.inventory;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MovementRequest {

    @NotNull(message = "L'ID du produit est obligatoire")
    private Long productId;

    @NotNull(message = "L'ID de l'entrepot est obligatoire")
    private Long warehouseId;

    @Positive(message = "La quantité doit étre supérieur à zéro")
    @NotNull(message = "La quantité est obligatoire")
    private Integer quantity;
}
