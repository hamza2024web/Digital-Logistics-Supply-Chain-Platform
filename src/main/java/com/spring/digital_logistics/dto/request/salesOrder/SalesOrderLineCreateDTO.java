package com.spring.digital_logistics.dto.request.salesOrder;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class SalesOrderLineCreateDTO {

    @NotNull(message = "L'ID du produit est obligatoire")
    private Long productId;

    @NotNull(message = "la quantity est obligatoire")
    @Positive(message = "La quantity doit étre supérieure à zéro")
    private Integer quantity;

}
