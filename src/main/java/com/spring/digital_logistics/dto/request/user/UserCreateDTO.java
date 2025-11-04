package com.spring.digital_logistics.dto.request.user;

import lombok.Data;
import jakarta.validation.constraints.*;

@Data
public class UserCreateDTO {
    @NotBlank(message = "le prénom est obligatoire")
    private String firstName;

    @NotBlank(message = "le nom est obligatoire")
    private String lastName;

    @Email(message = "L'email doit étre valide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @NotBlank(message = "Le mot de pass est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;
}
