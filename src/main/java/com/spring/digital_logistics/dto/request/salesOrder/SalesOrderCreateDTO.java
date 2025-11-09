package com.spring.digital_logistics.dto.request.salesOrder;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SalesOrderCreateDTO {

    @NotNull(message = "L'ID de l'entrepôt source est obligatoire")
    private Long warehouseId;

    @NotEmpty(message = "La commande doit contenir au moins une ligne de produit.")
    @Valid
    private List<SalesOrderLineCreateDTO> lines;
}
