package com.spring.digital_logistics.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.digital_logistics.IntegrationTestBase;
import com.spring.digital_logistics.dto.request.login.LoginDTO;
import com.spring.digital_logistics.dto.request.user.UserCreateDTO;
import com.spring.digital_logistics.entity.enums.Role;
import com.spring.digital_logistics.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthControllerIT extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserService userService;

    @BeforeEach
    void setUp(){
        UserCreateDTO user = new UserCreateDTO();
        user.setEmail("test@test.com");
        user.setPassword("password123");
        user.setFirstName("Test");
        user.setLastName("User");

        try {
            userService.register(user);
        } catch (Exception e){

        }
    }

    @Test
    public void shouldLoginSuccessfully() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("test@test.com");
        loginDTO.setPassword("password123");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token",notNullValue()))
                .andExpect(jsonPath("$.refreshToken",notNullValue()))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    public void shouldFailLoginWithBadCredentials() throws Exception {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setEmail("test@test.com");
        loginDTO.setPassword("wrongPassword");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginDTO)))
                .andExpect(status().isUnauthorized());
    }


}
