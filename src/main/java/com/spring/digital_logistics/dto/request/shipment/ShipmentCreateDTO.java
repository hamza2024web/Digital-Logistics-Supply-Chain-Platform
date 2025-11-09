package com.spring.digital_logistics.dto.request.shipment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ShipmentCreateDTO {
    @NotBlank(message = "Le numéro de suivi est obligatoire.")
    private String trackingNumber;
}