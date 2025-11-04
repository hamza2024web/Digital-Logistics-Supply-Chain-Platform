package com.spring.digital_logistics.dto.request.adjustement;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdjustmentRequestDTO {

    @NotNull(message = "L'ID du produit est obligatoire")
    private Long productId;

    @NotNull(message = "L'ID de l'entrepot est obligatoire")
    private Long warehouseId;

    @NotNull(message = "La quantité d'ajustement est obligatoire")
    private Integer quantity;

    @NotBlank(message = "La raison de l'ajustement est obligatoire")
    private String reason;
}
