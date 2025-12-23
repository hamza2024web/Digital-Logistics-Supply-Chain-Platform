package com.spring.digital_logistics.dto.response.login;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {
    private String token;
    private String refreshToken;
    private String email;
    private String role;
}
