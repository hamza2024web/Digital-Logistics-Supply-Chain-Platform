package com.spring.digital_logistics.controller;

import com.spring.digital_logistics.IntegrationTestBase;
import com.spring.digital_logistics.dto.request.warehouse.WarehouseCreateDTO;
import com.spring.digital_logistics.repository.WarehouseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class WarehouseControllerIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @BeforeEach
    void setup(){
        warehouseRepository.deleteAll();
    }

    @Test
    @WithMockUser(authorities = "CLIENT")
    void createWarehouse_shouldReturn_StatusCreated() throws Exception {
        WarehouseCreateDTO createdWarehouse = new WarehouseCreateDTO();
        createdWarehouse.setName("Entrepot de Paris");
        createdWarehouse.setCode("Paris-001");

        mockMvc.perform(post("/api/admin/Warehouses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createdWarehouse)))
                .andExpect(status().isForbidden());
    }

}
