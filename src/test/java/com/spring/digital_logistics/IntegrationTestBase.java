package com.spring.digital_logistics;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Duration;

@SpringBootTest
@Testcontainers
public abstract class IntegrationTestBase {

    @Container
    static PostgreSQLContainer<?> database = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test")
            .withStartupTimeout(Duration.ofMinutes(2))
            .waitingFor(Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)));

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", database::getJdbcUrl);
        registry.add("spring.datasource.username", database::getUsername);
        registry.add("spring.datasource.password", database::getPassword);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.database-platform", () -> "org.hibernate.dialect.PostgreSQLDialect");

        registry.add("jwt.secret", () -> "une-fausse-cle-pour-les-tests-qui-fonctionne");
        registry.add("spring.docker.compose.enabled", () -> "false");
    }
}