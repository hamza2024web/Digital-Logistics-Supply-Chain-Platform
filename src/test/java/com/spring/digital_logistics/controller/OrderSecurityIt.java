package com.spring.digital_logistics.controller;

import com.spring.digital_logistics.IntegrationTestBase;
import com.spring.digital_logistics.entity.SalesOrder;
import com.spring.digital_logistics.entity.User;
import com.spring.digital_logistics.entity.Warehouse;
import com.spring.digital_logistics.entity.enums.Role;
import com.spring.digital_logistics.entity.enums.SalesOrderStatus;
import com.spring.digital_logistics.repository.SalesOrderRepository;
import com.spring.digital_logistics.repository.UserRepository;
import com.spring.digital_logistics.repository.WarehouseRepository;
import com.spring.digital_logistics.security.jwt.JwtUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class OrderSecurityIt extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SalesOrderRepository salesOrderRepository;

    @Autowired
    private WarehouseRepository warehouseRepository;

    @Autowired
    private JwtUtils jwtUtils;

    private String generateTokenForUser(String email){
        return jwtUtils.generateTokenFromUsername(email);
    }

    @Test
    public void shouldDenyAccessToOtherUserOrder() throws Exception {
        User hacker = new User();
        hacker.setEmail("hacker@test.com");
        hacker.setPassword("1234");
        hacker.setRole(Role.CLIENT);
        userRepository.save(hacker);

        User victim = new User();
        victim.setEmail("victim@test.com");
        victim.setPassword("1234");
        victim.setRole(Role.CLIENT);
        userRepository.save(victim);

        Warehouse warehouse = new Warehouse();
        warehouse.setCode("Paris-001");
        warehouse.setName("Entrepot de paris");
        warehouseRepository.save(warehouse);

        SalesOrder order = new SalesOrder();
        order.setClient(victim);
        order.setCreatedAt(LocalDateTime.now());
        order.setStatus(SalesOrderStatus.CREATED);
        order.setWarehouse(warehouse);
        order = salesOrderRepository.save(order);
        Long orderId = order.getId();

        String hackerToken = generateTokenForUser("hacker@test.com");

        mockMvc.perform(get("/api/client/orders/" + orderId)
                .header("Authorization","Bearer " + hackerToken))
                .andExpect(status().isForbidden());
    }
}
