package com.spring.digital_logistics.dto.response.supplier;

import lombok.Data;

@Data
public class SupplierDTO {
    private Long id;
    private String name;
    private String contactPerson;
    private String email;
    private String phone;
}
