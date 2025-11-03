package com.spring.digital_logistics.dto.response.user;

import com.spring.digital_logistics.entity.enums.Role;
import lombok.Data;

@Data
public class UserDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private boolean isActive;
    private Role role;
}
