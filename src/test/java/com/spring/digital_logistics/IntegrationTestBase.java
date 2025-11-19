package com.spring.digital_logistics;

import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@Testcontainers
public abstract class IntegrationTestBase {

    static PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        database.start();

        String jdbcUrl = String.format("jdbc:postgresql://host.docker.internal:%d/%s",
                database.getMappedPort(5432),
                database.getDatabaseName());

        registry.add("spring.datasource.url", () -> jdbcUrl);
        registry.add("spring.datasource.username", database::getUsername);
        registry.add("spring.datasource.password", database::getPassword);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("jwt.secret", () -> "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBsb2dpc3RpY3MuY29tIiwiaWF0IjoxNzYzMzg5NDk0LCJleHAiOjE3NjM0NzU4OTR9.lzR2e_cYf_YwcmAyihvRgQ_kLS2J1JVpn4pbR3KYWpI");
    }
}