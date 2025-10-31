package com.spring.digital_logistics.dto.response;

import com.spring.digital_logistics.entity.enums.Role;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String firstname;
    private String lastname;
    private String email;
    private boolean isActive;
    private Role role;
}
