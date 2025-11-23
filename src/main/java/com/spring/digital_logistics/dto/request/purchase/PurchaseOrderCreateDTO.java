package com.spring.digital_logistics.dto.request.purchase;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PurchaseOrderCreateDTO {
    @NotNull(message = "L'ID du fournisseur est obligatoire")
    private Long supplierId;

    @NotNull(message = "L'ID de l'entrepôt de destination est obligatoire")
    private Long destinationWarehouseId;

    @NotEmpty(message = "Une commande doit contenir au moins un produit")
    @Valid
    private List<PurchaseOrderLineCreateDTO> lines;
}
