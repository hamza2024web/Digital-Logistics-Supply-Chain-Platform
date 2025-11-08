package com.spring.digital_logistics.dto.request.salesOrder;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SalesOrderRequestDTO {

    @NotNull(message = "L'ID du client est obligatoire")
    private Long clientId;

    @NotNull(message = "L'ID de l'entrepôt est obligatoire")
    private Long warehouseId;
}
