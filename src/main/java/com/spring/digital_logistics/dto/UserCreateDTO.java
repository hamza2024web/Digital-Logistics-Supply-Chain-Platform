package com.spring.digital_logistics.dto;

import com.spring.digital_logistics.entity.enums.Role;
import lombok.Data;

@Data
public class UserCreateDTO {
    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private boolean isActive;
    private Role role;
}
