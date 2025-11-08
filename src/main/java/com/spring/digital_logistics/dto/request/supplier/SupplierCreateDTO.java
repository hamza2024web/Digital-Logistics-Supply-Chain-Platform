package com.spring.digital_logistics.dto.request.supplier;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupplierCreateDTO {
    @NotBlank(message = "Le nom du fournisseur est obligatoire")
    private String name;

    private String contactPerson;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Le format de l'email est invalide")
    private String email;

    private String phone;
}
